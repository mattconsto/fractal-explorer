package mc21g14.fractal.calculable;

import static org.jocl.CL.*;

import java.awt.Dimension;
import java.io.*;

import mc21g14.fractal.misc.FractalLocation;
import mc21g14.fractal.misc.FractalState;

import org.jocl.*;

/**
 * OpenCL calculation backend.
 * 
 * @author Matthew Consterdine
 */
public class OpenCLCalculator extends Calculable {
	protected cl_context	   context;
	protected cl_command_queue queue;
	protected cl_program	   program;
	protected cl_kernel		kernel;

	private FractalState stateCheck = null;
	
	public OpenCLCalculator() throws IOException {
		// Enable exceptions and subsequently omit error checks in this sample
		CL.setExceptionsEnabled(true);
		
		// Obtain a platform ID
		int numPlatformsArray[] = new int[1];
		clGetPlatformIDs(0, null, numPlatformsArray);
		int numPlatforms = numPlatformsArray[0];
		cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
		clGetPlatformIDs(platforms.length, platforms, null);
		cl_platform_id platform = platforms[0];
		
		// Initialise the context properties
		cl_context_properties properties = new cl_context_properties();
		properties.addProperty(CL_CONTEXT_PLATFORM, platform);
		
		// Obtain a device ID
		int numDevicesArray[] = new int[1];
		clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
		cl_device_id devices[] = new cl_device_id[numDevicesArray[0]];
		clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, numDevicesArray[0], devices, null);
		cl_device_id device = devices[0];
		
		// Create a context and command queue for the selected device
		context = clCreateContext(properties, 1, new cl_device_id[] {device}, null, null, null);
		queue   = clCreateCommandQueue(context, device, 0, null);
	}
	
	@Override
	public String[] getImplementedFractals() {
		return new String[] {"Mandlebrot", "Burning Ship", "Tricorn", "Nova", "Circle"};
	}

	protected void generateKernel(FractalState state) {
		// Load the program
//		String[] lines = null;
//
//		// Try to load the program from the jar, if that fails try loading from current directory,
//		// if that fails throw an exception so the rest of the program can handle it
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/calc.cl")));
//			StringBuilder  builder = new StringBuilder();
//
//			String line = "";
//			while((line = reader.readLine()) != null) builder.append(line).append("\n");
//			lines = new String[] {builder.toString()};
//		} catch(Exception f) {
//
//		}
//
//		// Create the kernel
//		program = clCreateProgramWithSource(context, 1, lines, null, null);
//		clBuildProgram(program, 0, null, null, null, null);
//		kernel  = clCreateKernel(program, "fractalKernel", null);

		// Create the kernel
		System.out.println("Generating kernel");
		String code = OpenCLBuilder.build(state);
//		System.out.println(code);
		if(program != null) clReleaseProgram(program);
		program = clCreateProgramWithSource(context, 1, new String[] {code}, null, null);
		clBuildProgram(program, 0, null, null, null, null);
		if(kernel != null) clReleaseKernel(kernel);
		kernel  = clCreateKernel(program, "fractalKernel", null);
	}
	
	@Override
	protected double[] calculate(FractalState state, FractalLocation location, Dimension size) {
		if(state.fractal == null) state = state.setFractal(getImplementedFractals()[0]);

		if(state != stateCheck) {
			System.out.println((state != null ? state.hashCode() : null) + "!=" + (stateCheck != null ? stateCheck.hashCode() : "null"));
			stateCheck = state;
			generateKernel(state);
		}

		double[] results = new double[size.width * size.height];
		
		int orbitIndex = 0;
		String[] orbits	 = new String[] {"None", "Cross", "Dots"};
		for(int i = 0; i < orbits.length; i++) {
			if(orbits[i].equals(state.orbitTraps)) {
				orbitIndex = i;
				break;
			}
		}
		
		int regionIndex = 0;
		String[] regions = new String[] {"None", "Iterations", "Axis"};
		for(int i = 0; i < regions.length; i++) {
			if(regions[i].equals(state.regionSplits)) {
				regionIndex = i;
				break;
			}
		}
		
		// Allocate the memory objects for the input- and output data
		cl_mem mem[] = new cl_mem[] {
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{location.start}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{location.end}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{location.top}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{location.bottom}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{size.width}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{size.height}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{state.iterations}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{state.threshold}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{state.seed != null ? state.seed.r : Double.MAX_VALUE}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{state.seed != null ? state.seed.i : Double.MAX_VALUE}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{state.order}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{orbitIndex}), null),
			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{regionIndex}), null),
			clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_double * size.width * size.height, null, null)

//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{location.start}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{location.end}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{location.top}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{location.bottom}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{size.width}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{size.height}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{state.iterations}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{state.threshold}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{state.smooth ? 1 : 0}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{state.seed != null ? state.seed.r : Double.MAX_VALUE}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double, Pointer.to(new double[]{state.seed != null ? state.seed.i : Double.MAX_VALUE}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{0}), null),//selected
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{state.order}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{state.inverse ? 1 : 0}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{state.buddha ? 1 : 0}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{orbitIndex}), null),
//			clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,	Pointer.to(new int   []{regionIndex}), null),
//			clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_double * size.width * size.height, null, null)
		};
		
		// Set the arguments for the kernel
		for(int i = 0; i < mem.length; i++) clSetKernelArg(kernel, i, Sizeof.cl_mem, Pointer.to(mem[i]));
		
		// Execute the kernel
		clEnqueueNDRangeKernel(queue, kernel, 1, null, new long[] {size.width * size.height}, null, 0, null, null);
		
		// Read the output data
		clEnqueueReadBuffer(queue, mem[mem.length - 1], CL_TRUE, 0, Sizeof.cl_double * size.width * size.height, Pointer.to(results), 0, null, null);
		
		// Release kernel, program, and memory objects
		for(cl_mem m : mem) clReleaseMemObject(m);
		
		return results;
	}
	
	protected void finalize() throws Throwable {
		// OpenCL requires a cleanup after it runs
		super.finalize();
		clReleaseCommandQueue(queue);
		clReleaseContext(context);
		clReleaseKernel(kernel);
		clReleaseProgram(program);
	}
}
