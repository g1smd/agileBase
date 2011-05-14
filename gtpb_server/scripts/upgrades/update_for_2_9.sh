## In uploads folder
find . -iname '*.jpg' > allfiles.txt
cat allfiles.txt | xargs -l -i gm convert -resize '40x40>' {} {}.40.jpg
cat allfiles.txt | xargs -l -i gm convert -resize '500x500>' {} {}.500.jpg