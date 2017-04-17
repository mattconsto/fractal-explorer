# README

Built using: https://github.com/mattconsto/lafortuna

1. Connect La Fortuna in DFU mode
2. Build using `make`
3. Reset La Fortuna
4. Explore fractals using rotary encoder and arrows
5. Press the enter button to change fractal

## More fractals

Press the center button to change the fractal between:
* Mandlebrot
* Burning Ship
* Tricorn
* Nova
* Circle

Continued pressing results in the following behavior:
(Presses / 5)  % 2 - Inverse the base
(Presses / 10) % 2 - Enable orbit traps
(Presses / 15) % 2 - Enable region splits (Requires orbit traps)

If you want to change the order or view a julia set, you need to edit main.c

## Other notes

* Reset the device to reset your current position.
* The renderer uses the Adam7 interlacing algorithm to quickly generate an over-
  view of the fractal, then to progressively render in finer detail without rep-
  eating work.
