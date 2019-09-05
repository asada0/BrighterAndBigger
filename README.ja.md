# 明るく大きく

**「明るく大きく」** Android version 2.0のソースコードです。

<p align="center">
<img src="https://asada.website/brighterandbigger/favicon.ico">
</p>

## 「明るく大きく」とは?

- 「明るく大きく」は、老視や眼の病気などで小さな文字などが読みにくい人のための読字補助ツールです。

- 人は誰でも加齢と共に眼の水晶体の機能が衰え、小さな字が読みにくく、視界も暗くなります。また、白内障や弱視など、眼の病気が原因で文字が読みにくくなることもあります。
- このアプリは、小さな文字を、色彩処理により「明るく」、「大きく」、「くっきり」とさせて、快適に読むための補助をします。

<p align="center">
<img src="https://asada.website/brighterandbigger/my_images/BootA-j2.0.jpg" width="240">   
</p>

## 特徴

- 読みにくい文字を、「明るく」、「大きく」、「くっきり」と表示することができます。

- 色彩処理により、元の色をなるべく保ったまま、「明るく」「くっきり」とすることができます。
- 白内障や弱視などの人の読みやすさ向上のために、黒字に白などで表示する「明度反転」モードや、表示色を限定する「モノクローム」モードを備えています。
- 斜め上から本などを読むときに、歪みを補正して読みやすくする「斜め上から補正」をサポートしました。
- ぼやけながらも遠くを見るための単眼鏡的な使い方をするために、最大倍率は20倍までサポートしています。

<p align="center">
<img src="https://asada.website/brighterandbigger/my_images/HomeA-j2.0.jpg" width="360">   
<img src="https://asada.website/brighterandbigger/my_images/HomeA2-j2.0.jpg" width="360"> 
</p>

## アナウンス
オープンソース化に当たってのアナウンスはこちら。  
<https://asada.website/brighterandbigger/j/opensource.html>

## 公式ウェブサイト
「明るく大きく」の公式ウェブサイトはこちら。  
<https://asada.website/brighterandbigger/>

## マニュアル
このアプリのマニュアルは公式サイトにあります。  
<https://asada.website/brighterandbigger/j/manual.html>

## 動作条件

Android 5.0 (API level 21) 以上、 OpenGL ES2.0以上を搭載するAndroidデバイス。  
Android Studio。

## ノート

このアプリは、浅田 一憲（医学・メディアデザイン学）によって開発されました。

## 謝辞
本バージョンの開発にあたって、多くの時間と労力を使ってお手伝いいただいた盟友の鵜川 裕文さん、松田 雅孝さん、そしてテストに協力してくださった全ての友人に感謝申し上げます。

## ライセンス
### The MIT License (MIT)  

Copyright 2019 Kazunori Asada

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

### Powered by
NumberPicker - Copyright 2018 ShawnLin013 (MIT license).

## 付録
### 本アプリケーションで使用している明るくくっきり理論の数式 

(1) sRGB(gammaed)からsRGB(linear)へRGB色の変換。 (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{matrix}&space;R_{linear}=\left&space;(&space;\frac{R_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}\\\\&space;G_{linear}=\left&space;(&space;\frac{G_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}\\\\&space;B_{linear}=\left&space;(&space;\frac{B_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}&space;\end{matrix}" title="\begin{matrix} R_{linear}=\left ( \frac{R_{device}+0.055}{1.055} \right )^{2.4}\\\\ G_{linear}=\left ( \frac{G_{device}+0.055}{1.055} \right )^{2.4}\\\\ B_{linear}=\left ( \frac{B_{device}+0.055}{1.055} \right )^{2.4} \end{matrix}" />

(2) sRGBからCIEXYZへ色空間の変換。 (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0.4124&space;&&space;0.3576&space;&&space;0.1805\\&space;0.2126&space;&&space;0.7152&space;&&space;0.0722\\&space;0.0193&space;&&space;0.1192&space;&&space;0.9595&space;\end{pmatrix}&space;\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}" title="\begin{pmatrix} X\\ Y\\ Z \end{pmatrix} = \begin{pmatrix} 0.4124 & 0.3576 & 0.1805\\ 0.2126 & 0.7152 & 0.0722\\ 0.0193 & 0.1192 & 0.9595 \end{pmatrix} \begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix}" />

(3) CIEXYZからCIELUVへ色空間の変換。 (CIE 1976 L\*u\*v\* Color Space) 

<img src="https://latex.codecogs.com/gif.latex?L^{*}=\begin{cases}&space;9.033\frac{Y}{Y_{n}}&space;&&space;\text{&space;if&space;}&space;\frac{Y}{Y_{n}}\leq0.0088565&space;\\&space;1.16(\frac{Y}{Y_{n}})^{\frac{1}{3}}-0.16&&space;\text{&space;if&space;}&space;\frac{Y}{Y_{n}}&gt;0.0088565&space;\end{cases}" title="L^{*}=\begin{cases} 9.033\frac{Y}{Y_{n}} & \text{ if } \frac{Y}{Y_{n}}\leq0.0088565 \\ 1.16(\frac{Y}{Y_{n}})^{\frac{1}{3}}-0.16& \text{ if } \frac{Y}{Y_{n}}&gt;0.0088565 \end{cases}" />

（注意：<img src="https://latex.codecogs.com/gif.latex?0&space;\leq&space;L^{*}\leq&space;1" title="0 \leq L^{*}\leq 1" />としている。100ではない。）

<img src="https://latex.codecogs.com/gif.latex?u^{*}&space;=&space;13L^{*}\cdot&space;(u^{'}-u_{n}^{'})" title="u^{*} = 13L^{*}\cdot (u^{'}-u_{n}^{'})" />

<img src="https://latex.codecogs.com/gif.latex?v^{*}&space;=&space;13L^{*}\cdot&space;(v^{'}-v_{n}^{'})" title="v^{*} = 13L^{*}\cdot (v^{'}-v_{n}^{'})" />

D65光源では、

<img src="https://latex.codecogs.com/gif.latex?Y_{n}=1" title="Y_{n}=1" />

 <img src="https://latex.codecogs.com/gif.latex?(u_{n}^{'},&space;v_{n}^{'})=(0.19784,&space;0.46832)" title="(u_{n}^{'}, v_{n}^{'})=(0.19784, 0.46832)" />

ただし、

<img src="https://latex.codecogs.com/gif.latex?u^{'}=\frac{4X}{X&plus;15Y&plus;3Z}" title="u^{'}=\frac{4X}{X+15Y+3Z}" />

<img src="https://latex.codecogs.com/gif.latex?v^{'}=\frac{9Y}{X&plus;15Y&plus;3Z}" title="v^{'}=\frac{9Y}{X+15Y+3Z}" />

(4) 明るく。CIELUV均等色空間において、明度 L\* のみを変更することによって明るくする。色度は変更しない。「明るく」ダイヤルの値 brightness に応じて明度 L\* を増加（明るく）減少（暗く）する。

<img src="https://latex.codecogs.com/gif.latex?L_{bright}^{*}=L_{original}^{*}&space;&plus;&space;brightness" title="L_{bright}^{*}=L^{*} + brightness" />

( 本アプリケーションでは、<img src="https://latex.codecogs.com/gif.latex?-0.25\leq&space;brightness\leq&space;0.25" title="-0.25\leq brightness\leq 0.25" /> )

![result](https://asada.website/brighterandbigger/my_images/gif-brighter.gif)

（付図1）明るくダイヤルを回す（横軸が変更前、縦軸が変更後の明度）
<br />
<br />

(5) くっきり。CIELUV均等色空間において、明度 L\* のみを変更することによってくっきりさせる。色度は変更しない。「くっきり」ダイヤルの値 contrast に応じて明度 L\* の傾きを増加（コントラスをを高く）減少（コントラストを低く）する。なめらかに変化するようにシグモイド関数を使用している。

<img src="https://latex.codecogs.com/gif.latex?t_{0}&space;=&space;20\cdot&space;contrast&plus;1" title="t_{0} = 20\cdot contrast+1" />

<img src="https://latex.codecogs.com/gif.latex?y_{0}&space;=&space;\frac{1}{1&space;&plus;&space;e^{t_{0}}}" title="y_{0} = \frac{1}{1 + e^{t_{0}}}" />

<img src="https://latex.codecogs.com/gif.latex?y_{1}&space;=&space;\frac{1}{1&space;&plus;&space;e^{(1-2L_{bright}^{*})\cdot&space;t_{0}}}" title="y_{1} = \frac{1}{1 + e^{1-2\cdot L_{bright}^{*}\cdot t_{0}}}" />

<img src="https://latex.codecogs.com/gif.latex?L_{clear}^{*}=\frac{y_{1}&space;-&space;y_{0}}{1-2y_{0}}" title="L_{clear}^{*}=\frac{y_{1} - y_{0}}{1-2y_{0}}" />

( 本アプリケーションでは、<img src="https://latex.codecogs.com/gif.latex?-1\leq&space;contrast\leq&space;1" title="-1\leq contrast\leq 1" /> )

![result](https://asada.website/brighterandbigger/my_images/gif-clearer.gif)

（付図2）くっきりダイヤルを回す（横軸が変更前、縦軸が変更後の明度）
<br />
<br />

![result](https://asada.website/brighterandbigger/my_images/gif-brighterclearer.gif)

（付図3）くっきり後に明るくダイヤルを回す（横軸が変更前、縦軸が変更後の明度）
<br />
<br />


(6) 明度反転

<img src="https://latex.codecogs.com/gif.latex?L_{reverse}^{*}=1-L_{original}^{*}" title="L_{reverse}^{*}=1-L_{original}^{*}" />

(7) モノクロームモード

(7-1) モノクロームモードでは元のピクセルの色度は使用せず、明度だけを使用する。
まず、使用するカラーc1（明）とカラーc2（暗）のu'v'色度図（CIE 1976 UCS色度図）における色度
<img src="https://latex.codecogs.com/gif.latex?(u_{c1}^{'},&space;v_{c1}^{'}),(u_{c2}^{'},&space;v_{c2}^{'})" title="(u_{c1}^{'}, v_{c1}^{'}),(u_{c2}^{'}, v_{c2}^{'})" />
を求める。1色（c1）のみの場合はc2を無彩色
<img src="https://latex.codecogs.com/gif.latex?(u_{n}^{'},&space;v_{n}^{'})" title="(u_{n}^{'}, v_{n}^{'})" />
とする。RGBからXYZへの変換は式1, 式2を、u'v'色度への変換には式3（下）を使用する。

(7-2) 変換前の色の明度 L\* はキープし、色度のみを変える。ある色c0の明度が L<sub>c0</sub>\* とすると、その色度 (u<sub>c0new</sub>', v<sub>c0new</sub>') は、u'v'色度図平面上において、c1とc2の色度を L<sub>c0</sub>\* の値に応じて線形補間することによって求める。

<img src="https://latex.codecogs.com/gif.latex?(u_{c0new}^{'},&space;v_{c0new}^{'})=L_{c0}^{*}(u_{c1}^{'},&space;v_{c1}^{'})&plus;(1-L_{c0}^{*})(u_{c2}^{'},&space;v_{c2}^{'})" title="(u_{conew}^{'}, v_{c0new}^{'})=L_{c0}^{*}(u_{c1}^{'}, v_{c1}^{'})+(1-L_{c0}^{*})(u_{c2}^{'}, v_{c2}^{'})" />

(8) CIELUVからXYZに色空間を変換。 (CIE 1976 L\*u\*v\* Color Space) 

<img src="https://latex.codecogs.com/gif.latex?Y=\begin{cases}&space;Y_{n}\cdot&space;L^{*}\cdot&space;0.11071&space;&&space;\text{&space;if&space;}&space;L^{*}\leq&space;0.08&space;\\&space;Y_{n}\cdot&space;(\frac{L^{*}&plus;0.16}{1.16})^{3}&space;&&space;\text{&space;if&space;}&space;L^{*}&gt;0.08&space;\end{cases}" title="Y=\begin{cases} Y_{n}\cdot L^{*}\cdot 0.11071 & \text{ if } L^{*}\leq 0.08 \\ Y_{n}\cdot (\frac{L^{*}+0.16}{1.16})^{3} & \text{ if } L^{*}&gt;0.08 \end{cases}" />

<img src="https://latex.codecogs.com/gif.latex?X=Y\cdot&space;\frac{9u^{'}}{4v^{'}}" title="X=Y\cdot \frac{9u^{'}}{4v^{'}}" />

<img src="https://latex.codecogs.com/gif.latex?Z=Y\cdot&space;\frac{12-3u^{'}-20v^{'}}{4v^{'}}" title="Z=Y\cdot \frac{12-3u^{'}-20v^{'}}{4v^{'}}" />

(9) CIEXYZからsRGBへ色空間の変換。 (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;3.2406&space;&&space;-1.5372&space;&&space;-0.4986\\&space;-0.9689&space;&&space;1.8758&space;&&space;0.0415\\&space;0.0557&space;&&space;-0.2040&space;&&space;1.0570&space;\end{pmatrix}&space;\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}" title="\begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix} = \begin{pmatrix} 3.2406 & -1.5372 & -0.4986\\ -0.9689 & 1.8758 & 0.0415\\ 0.0557 & -0.2040 & 1.0570 \end{pmatrix} \begin{pmatrix} X\\ Y\\ Z \end{pmatrix}" />

(10) sRGB(linear)からsRGB(gammaed)へのRGB色の変換。 (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{matrix}&space;R_{device}=1.055R_{linear}^{\frac{1}{2.4}}-0.055\\\\&space;G_{device}=1.055G_{linear}^{\frac{1}{2.4}}-0.055\\\\&space;B_{device}=1.055B_{linear}^{\frac{1}{2.4}}-0.055&space;\end{matrix}" title="\begin{matrix} R_{device}=1.055R_{linear}^{\frac{1}{2.4}}-0.055\\\\ G_{device}=1.055G_{linear}^{\frac{1}{2.4}}-0.055\\\\ B_{device}=1.055B_{linear}^{\frac{1}{2.4}}-0.055 \end{matrix}" />

(11) 斜め上から補正（射影変換）。OpenGLのtexture2DProj()を使用して実現しているが、数式にすると以下になるはず。

<img src="https://latex.codecogs.com/gif.latex?z&space;=&space;(1-\frac{1}{ratio})y_{original}&plus;\frac{1}{ratio}" title="z = (1-\frac{1}{ratio})y_{original}+\frac{1}{ratio}" />

<img src="https://latex.codecogs.com/gif.latex?x_{projection}&space;=&space;\frac{x_{original}&plus;0.5(1-\frac{1}{ratio})y_{original}-0.5(1-\frac{1}{ratio})}{z}" title="x_{projection} = \frac{x_{original}+0.5(1-\frac{1}{ratio})y_{original}-0.5(1-\frac{1}{ratio})}{z}" />

<img src="https://latex.codecogs.com/gif.latex?y_{projection}&space;=\frac{y_{original}}{z}" title="y_{projection} =\frac{y_{original}}{z}" />

( 本アプリケーションでは、<img src="https://latex.codecogs.com/gif.latex?0.2\leq&space;ratio\leq&space;1" title="0.2\leq contrast\leq 1" /> )

<p>
<img src="https://asada.website/brighterandbigger/my_images/Home4-j2.0.jpg" width="360">  
<img src="https://asada.website/brighterandbigger/my_images/Home5-j2.0.jpg" width="360">  
</p>
