MagStripeCard
-------------

| Tag No | Factor No | 名称                          |
|--------|-----------|-------------------------------|
| 3      | 1         | Success times for stripe card |
| 3      | 2         | Success times for track 1     |
| 3      | 3         | Fail times for track 1        |
| 3      | 4         | Success times for track 2     |
| 3      | 5         | Fail times for track 2        |
| 3      | 6         | Success times for track 3     |
| 3      | 7         | Fail times for track 3        |

Contactless Card 
-----------------

| Tag No | Factor No | 名称                                        |
|--------|-----------|---------------------------------------------|
| 5      | 3         | Success times for Contactless Card activate |
| 5      | 4         | Fail times for Contactless Card activate    |
| 5      | 5         | Fail times for APDU with Contactless Card   |

IC Card
-------

| Tag No | Factor No | 名称                               |
|--------|-----------|------------------------------------|
| 4      | 3         | Success times for IC Card power up |
| 4      | 4         | Fail times for IC Card power up    |
| 4      | 5         | Fail times for APDU with IC Card   |

Pinpad
------

| Tag No | Factor No | 名称                               |
|--------|-----------|------------------------------------|
| 6      | 1         | The number of Offline PIN input    |
| 6      | 2         | The number of Online PIN input     |
| 6      | 3         | The number of secret key injection |
| 6      | 5         | The number of Non PIN input        |

Printer
-------

| Tag No | Factor No | 名称                          |
|--------|-----------|-------------------------------|
| 7      | 1         | The number of Over Heat       |
| 7      | 2         | The number of Low Temperature |
| 7      | 3         | Steps of printer motor        |

Security Module
---------------

| Tag No | Factor No | 名称                   |
|--------|-----------|------------------------|
| 20     | 1         | Times of clear warning |
