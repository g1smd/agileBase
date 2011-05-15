## In uploads folder
find . -iname '*.jpg' > jpg.txt
find . -iname '*.png' > png.txt
cat jpg.txt | xargs -l -i convert -resize '40x40>' {} {}.40.jpg
cat jpg.txt | xargs -l -i gm convert -resize '500x500>' {} {}.500.jpg
cat png.txt | xargs -l -i convert -resize '40x40>' {} {}.40.png
cat png.txt | xargs -l -i gm convert -resize '500x500>' {} {}.500.png
