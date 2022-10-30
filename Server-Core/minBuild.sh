#
# Copyright 2020-2022 RW-HPS Team and contributors.
#
# 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
# Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
#
# https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
#


#java -version
#gradle jar

cur_dir=$(pwd)
echo $cur_dir

ramDisk="/mnt/r"
tempUnzip=$ramDisk"/tempUnzip"
mkdir $tempUnzip

echo "Unzip Jar"
unzip build/libs/Server.jar -q -d $tempUnzip

cd $tempUnzip

echo "Min Jar"
find . -type f -iname \*.kotlin_metadata -delete
find . -type d -empty | xargs -n 1 rm -rf
find . -type d -empty | xargs -n 1 rm -rf
find . -type d -empty | xargs -n 1 rm -rf
find . -type d -empty | xargs -n 1 rm -rf
find . -type d -empty | xargs -n 1 rm -rf

echo "Zip Jar"
zip -9 -q -r Server.jar *

mv Server.jar $cur_dir"/build/libs/"MinServer.jar

cd ..
rm -rf $tempUnzip

