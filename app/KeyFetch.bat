echo 'SHA-1 Áö¹® È¹µæ'
SET JAVA_HOME=C:\Program Files\Java\jdk1.8.0_65\bin
SET PATH=PATH;%JAVA_HOME%

keytool -list -v -keystore release_gmt.jks

pause