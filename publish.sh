
echo ====================================
echo "Exotic Matter"
echo ====================================
./gradlew publish

cd builds

echo ====================================
echo "Exotic Art Core"
echo ====================================
cd core
../../gradlew publish
cd ..

echo ====================================
echo "Exotic Art Test"
echo ====================================
cd test
../../gradlew publish
cd ..

echo ====================================
echo "Exotic Art Unstable"
echo ====================================
cd unstable
../../gradlew publish
cd ..

cd ..
