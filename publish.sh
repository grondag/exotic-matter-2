
echo ====================================
echo "Exotic Matter"
echo ====================================
./gradlew publish --rerun-tasks
./gradlew build --rerun-tasks

cd builds

echo ====================================
echo "Exotic Art Core"
echo ====================================
cd core
../../gradlew publish --rerun-tasks
../../gradlew build --rerun-tasks
cd ..

echo ====================================
echo "Exotic Art Test"
echo ====================================
cd test
../../gradlew publish --rerun-tasks
../../gradlew build --rerun-tasks
cd ..

echo ====================================
echo "Exotic Art Tech"
echo ====================================
cd tech
../../gradlew publish --rerun-tasks
../../gradlew build --rerun-tasks
cd ..

echo ====================================
echo "Exotic Art Unstable"
echo ====================================
cd unstable
../../gradlew publish --rerun-tasks
../../gradlew build --rerun-tasks
cd ..

cd ..
