# Brighter and Bigger

This is an open source code of the **“Brighter and Bigger"** Android version 2.0.

<p align="center">
<img src="https://asada.website/brighterandbigger/favicon.ico">
</p>

## What is the "Brighter and Bigger"?

- A reading assistance tool for people who have difficulty reading small letters.

- "Brighter and Bigger" is a reading glasses tool for people who have difficulty reading fine print and focusing on nearby objects due to presbyopia, eye illness, etc.
- Presbyopia is a normal condition for people over the age of 40, which arises from age-related changes in the anatomy of the eye. Moreover, it becomes difficult to read small letters due to eye illnesses, such as cataracts and low vision.
- This application helps you to read small letters by making the images bigger, brighter and clearer with scientific color changing methods.

<p align="center">
<img src="http://asada.website/brighterandbigger/my_images/BootA-e2.0.jpg" width="240">　
</p>

## Features

- Shows you fine print bigger, brighter and clearer.

- "Brightness Reverse" mode and "Monochrome" mode are supported. These modes are suitable for people with cataracts and low vision.
- "Tilted Angle Correction" mode, which revises the distortion of images when you read books, etc. from a tilted angle, is supported.
- Maximum magnification of up to 20 times is supported so that you can use this tool as a monocle when you would like to see distant objects even if blurred.

<p align="center">
<img src="https://asada.website/brighterandbigger/my_images/HomeA-e2.0.jpg" width="360">   
<img src="https://asada.website/brighterandbigger/my_images/HomeA2-e2.0.jpg" width="360">
</p>

## Announcement
Announcement of making Open-Source version is here.  
<https://asada.website/brighterandbigger/e/opensource.html>

## Official Website
Official website of the "Chromatic Vision Simulator" is here.  
<https://asada.website/brighterandbigger/e/index.html>

## User's Guide
User's Guide of this application is in the official website.  
<https://asada.website/brighterandbigger/e/manual.html>

## Requirements

Android  device with Android 5.0 (API level 21) or later and OpenGL ES2.0 or later.

## Notes

This application software was developed by Kazunori Asada (Ph.D. of Medical Science and Ph.D. of Media design).

## Acknowledgment
I wish to express my gratitude to Mr. Hirofumi Ukawa and Masataka Matsuda who helped development of the open-source version.

## License
### The MIT License (MIT)  

Copyright 2019 Kazunori Asada

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

### Powered by
NumberPicker - Copyright 2018 ShawnLin013 (MIT license).

## Appendix
### Mathematical Expressions of Brighter and Clearer theory in this application 

(1) Convert RGB color from sRGB(gammaed) to sRGB(linear). (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{matrix}&space;R_{linear}=\left&space;(&space;\frac{R_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}\\\\&space;G_{linear}=\left&space;(&space;\frac{G_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}\\\\&space;B_{linear}=\left&space;(&space;\frac{B_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}&space;\end{matrix}" title="\begin{matrix} R_{linear}=\left ( \frac{R_{device}+0.055}{1.055} \right )^{2.4}\\\\ G_{linear}=\left ( \frac{G_{device}+0.055}{1.055} \right )^{2.4}\\\\ B_{linear}=\left ( \frac{B_{device}+0.055}{1.055} \right )^{2.4} \end{matrix}" />

(2) Convert color space from sRGB to CIEXYZ. (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0.4124&space;&&space;0.3576&space;&&space;0.1805\\&space;0.2126&space;&&space;0.7152&space;&&space;0.0722\\&space;0.0193&space;&&space;0.1192&space;&&space;0.9595&space;\end{pmatrix}&space;\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}" title="\begin{pmatrix} X\\ Y\\ Z \end{pmatrix} = \begin{pmatrix} 0.4124 & 0.3576 & 0.1805\\ 0.2126 & 0.7152 & 0.0722\\ 0.0193 & 0.1192 & 0.9595 \end{pmatrix} \begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix}" />

(3) Convert color space from CIEXYZ to CIELUV. (CIE 1976 L\*u\*v\* Color Space) 

<img src="https://latex.codecogs.com/gif.latex?L^{*}=\begin{cases}&space;9.033\frac{Y}{Y_{n}}&space;&&space;\text{&space;if&space;}&space;\frac{Y}{Y_{n}}\leq0.0088565&space;\\&space;1.16(\frac{Y}{Y_{n}})^{\frac{1}{3}}-0.16&&space;\text{&space;if&space;}&space;\frac{Y}{Y_{n}}&gt;0.0088565&space;\end{cases}" title="L^{*}=\begin{cases} 9.033\frac{Y}{Y_{n}} & \text{ if } \frac{Y}{Y_{n}}\leq0.0088565 \\ 1.16(\frac{Y}{Y_{n}})^{\frac{1}{3}}-0.16& \text{ if } \frac{Y}{Y_{n}}&gt;0.0088565 \end{cases}" />

(Where, <img src="https://latex.codecogs.com/gif.latex?0&space;\leq&space;L^{*}\leq&space;1" title="0 \leq L^{*}\leq 1" /> is assumed.)

<img src="https://latex.codecogs.com/gif.latex?u^{*}&space;=&space;13L^{*}\cdot&space;(u^{'}-u_{n}^{'})" title="u^{*} = 13L^{*}\cdot (u^{'}-u_{n}^{'})" />

<img src="https://latex.codecogs.com/gif.latex?v^{*}&space;=&space;13L^{*}\cdot&space;(v^{'}-v_{n}^{'})" title="v^{*} = 13L^{*}\cdot (v^{'}-v_{n}^{'})" />

When under D65 illuminant,

<img src="https://latex.codecogs.com/gif.latex?Y_{n}=1" title="Y_{n}=1" />

 <img src="https://latex.codecogs.com/gif.latex?(u_{n}^{'},&space;v_{n}^{'})=(0.19784,&space;0.46832)" title="(u_{n}^{'}, v_{n}^{'})=(0.19784, 0.46832)" />

Where, 

<img src="https://latex.codecogs.com/gif.latex?u^{'}=\frac{4X}{X&plus;15Y&plus;3Z}" title="u^{'}=\frac{4X}{X+15Y+3Z}" />

<img src="https://latex.codecogs.com/gif.latex?v^{'}=\frac{9X}{X&plus;15Y&plus;3Z}" title="v^{'}=\frac{9X}{X+15Y+3Z}" />

(4) Brighter. Only lightness L\* value is used for changing "Brighter" in CIELUV uniform color space. The chromaticity is not changed.
Increase (brighter) or decrease (darker) lightness L\* according to the value of the “Brighter Dial."

<img src="https://latex.codecogs.com/gif.latex?L_{bright}^{*}=L_{original}^{*}&space;&plus;&space;brightness" title="L_{bright}^{*}=L^{*} + brightness" />

![result](https://asada.website/brighterandbigger/my_images/gif-brighter.gif)

(fig.1) dial the Brighter. (The horizontal is the original, the vertical is the new lightness.)

(5) Clearer。Only lightness L\* value is used for changing "Clearer" in CIELUV uniform color space. The chromaticity is not changed. Increase or decrease contrast by changing the slope of lightness L\* according to the value of the “Clearer Dial." Sigmoid function is used to change contrast smoothly。

<img src="https://latex.codecogs.com/gif.latex?t_{0}&space;=&space;20\cdot&space;contrast&plus;1" title="t_{0} = 20\cdot contrast+1" />

<img src="https://latex.codecogs.com/gif.latex?y_{0}&space;=&space;\frac{1}{1&space;&plus;&space;e^{t_{0}}}" title="y_{0} = \frac{1}{1 + e^{t_{0}}}" />

<img src="https://latex.codecogs.com/gif.latex?y_{1}&space;=&space;\frac{1}{1&space;&plus;&space;e^{(1-2L_{bright}^{*})\cdot&space;t_{0}}}" title="y_{1} = \frac{1}{1 + e^{1-2L_{bright}^{*}\cdot t_{0}}}" />

<img src="https://latex.codecogs.com/gif.latex?L_{clear}^{*}=\frac{y_{1}&space;-&space;y_{0}}{1-2y_{0}}" title="L_{clear}^{*}=\frac{y_{1} - y_{0}}{1-2y_{0}}" />

![result](https://asada.website/brighterandbigger/my_images/gif-clearer.gif)

(fig.2) dial the Clearer. (The horizontal is the original, the vertical is the new lightness.)

![result](https://asada.website/brighterandbigger/my_images/gif-brighterclearer.gif)

(fig.3) dial the Brighter after Clearer. (The horizontal is the original, the vertical is the new lightness.)

(6) Monochrome Mode

(6-1) In the monochrome mode, only the lightness is used, not the chromaticity of the original pixel.
Obtain the chromaticity of the used color c1 (light) and c2 (dark)
<img src="https://latex.codecogs.com/gif.latex?(u_{c1}^{'},&space;v_{c1}^{'}),(u_{c2}^{'},&space;v_{c2}^{'})" title="(u_{c1}^{'}, v_{c1}^{'}),(u_{c2}^{'}, v_{c2}^{'})" />
in the u'v' chromaticity diagram (CIE1976UCS chromaticity diagram).
If there is only one color c1, c2 should be achromatic 
<img src="https://latex.codecogs.com/gif.latex?(u_{n}^{'},&space;v_{n}^{'})" title="(u_{n}^{'}, v_{n}^{'})" />.
Use Equation 1 and Equation 2 for conversion from RGB to XYZ, and use Equation 3 (bottom one) to convert to u'v' chromaticity.

(6-2) Change only the chromaticity,  keep the lightness L\* of the color before conversion. 
If the lightness of a certain color c0 is L<sub>c0</sub>\*, the chromaticity of c0 (u<sub>c0new</sub>', v<sub>c0new</sub>') is obtained by linearly interpolating the chromaticities of c1 and c2 on the u′v′ chromaticity diagram plane, according to the value of the lightness L<sub>c0</sub>\*.

<img src="https://latex.codecogs.com/gif.latex?(u_{c0new}^{'},&space;v_{c0new}^{'})=L_{c0}^{*}(u_{c1}^{'},&space;v_{c1}^{'})&plus;(1-L_{c0}^{*})(u_{c2}^{'},&space;v_{c2}^{'})" title="(u_{c0new}^{'}, v_{c0new}^{'})=L_{c0}^{*}(u_{c1}^{'}, v_{c1}^{'})+(1-L_{c0}^{*})(u_{c2}^{'}, v_{c2}^{'})" />

(7) Convert color space from CIELUV to CIEXYZ. (CIE 1976 L\*u\*v\* Color Space)

<img src="https://latex.codecogs.com/gif.latex?Y=\begin{cases}&space;Y_{n}\cdot&space;L^{*}\cdot&space;0.11071&space;&&space;\text{&space;if&space;}&space;L^{*}\leq&space;0.08&space;\\&space;Y_{n}\cdot&space;(\frac{L^{*}&plus;0.16}{1.16})^{3}&space;&&space;\text{&space;if&space;}&space;L^{*}&gt;0.08&space;\end{cases}" title="Y=\begin{cases} Y_{n}\cdot L^{*}\cdot 0.11071 & \text{ if } L^{*}\leq 0.08 \\ Y_{n}\cdot (\frac{L^{*}+0.16}{1.16})^{3} & \text{ if } L^{*}&gt;0.08 \end{cases}" />

<img src="https://latex.codecogs.com/gif.latex?X=Y\cdot&space;\frac{9u^{'}}{4v^{'}}" title="X=Y\cdot \frac{9u^{'}}{4v^{'}}" />

<img src="https://latex.codecogs.com/gif.latex?Z=Y\cdot&space;\frac{12-3u^{'}-20v^{'}}{4v^{'}}" title="Z=Y\cdot \frac{12-3u^{'}-20v^{'}}{4v^{'}}" />

(8) Convert color space from CIEXYZ to sRGB. (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;3.2406&space;&&space;-1.5372&space;&&space;-0.4986\\&space;-0.9689&space;&&space;1.8758&space;&&space;0.0415\\&space;0.0557&space;&&space;-0.2040&space;&&space;1.0570&space;\end{pmatrix}&space;\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}" title="\begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix} = \begin{pmatrix} 3.2406 & -1.5372 & -0.4986\\ -0.9689 & 1.8758 & 0.0415\\ 0.0557 & -0.2040 & 1.0570 \end{pmatrix} \begin{pmatrix} X\\ Y\\ Z \end{pmatrix}" />

(9) Convert RGB color from sRGB(linear) to sRGB(gammaed). (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{matrix}&space;R_{device}=1.055R_{linear}^{\frac{1}{2.4}}-0.055\\\\&space;G_{device}=1.055G_{linear}^{\frac{1}{2.4}}-0.055\\\\&space;B_{device}=1.055B_{linear}^{\frac{1}{2.4}}-0.055&space;\end{matrix}" title="\begin{matrix} R_{device}=1.055R_{linear}^{\frac{1}{2.4}}-0.055\\\\ G_{device}=1.055G_{linear}^{\frac{1}{2.4}}-0.055\\\\ B_{device}=1.055B_{linear}^{\frac{1}{2.4}}-0.055 \end{matrix}" />
