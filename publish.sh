
echo ====================================
echo "Exotic Matter"
echo ====================================
./gradlew publish
./gradlew build --rerun-tasks

cd builds

echo ====================================
echo "Exotic Art Core"
echo ====================================
cd core
../../gradlew publish
../../gradlew build --rerun-tasks
cd ..

echo ====================================
echo "Exotic Art Test"
echo ====================================
cd test
../../gradlew publish
../../gradlew build --rerun-tasks
cd ..

echo ====================================
echo "Exotic Art Tech"
echo ====================================
cd tech
../../gradlew publish
../../gradlew build --rerun-tasks
cd ..

echo ====================================
echo "Exotic Art Unstable"
echo ====================================
cd unstable
../../gradlew publish
../../gradlew build --rerun-tasks
cd ..

cd ..
