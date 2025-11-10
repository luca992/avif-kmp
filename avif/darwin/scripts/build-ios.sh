cd libavif || exit 255

# START dav1d
if ! [ -f ext/dav1d ]; then
  git clone -b 1.2.1 --depth 1 https://code.videolan.org/videolan/dav1d.git ext/dav1d
fi
cd ext/dav1d || exit 255

rm -rf "build"
mkdir "build"
cd "build" || exit 255

echo "ios_cross_file: ${IOS_CROSS_FILE}"
echo "ios_toolchain_cmake: ${IOS_TOOLCHAIN_FILE}"

meson setup \
  --cross-file="${IOS_CROSS_FILE}" \
  --default-library=static \
  --buildtype=release \
  -Db_lto=false \
  -Db_ndebug=false \
  -Denable_asm=false \
  -Denable_tools=false \
  -Denable_examples=false \
  -Denable_tests=false \
  ..
ninja

cd ..
cd ../..
# END dav1d

# START libsharpyuv (from libwebp)
if ! [ -d ext/libwebp ]; then
  echo "Error: ext/libwebp directory not found"
  exit 255
fi

cd ext/libwebp || exit 255

libsharpyuv_build_dir="build"
rm -rf "${libsharpyuv_build_dir}"
mkdir -p "${libsharpyuv_build_dir}"

cmake -B "${libsharpyuv_build_dir}" -G Xcode ${IOS_CMAKE_PARAMS} \
  -DCMAKE_BUILD_TYPE=Release \
  -DBUILD_SHARED_LIBS=OFF \
  -DWEBP_BUILD_ANIM_UTILS=OFF \
  -DWEBP_BUILD_CWEBP=OFF \
  -DWEBP_BUILD_DWEBP=OFF \
  -DWEBP_BUILD_GIF2WEBP=OFF \
  -DWEBP_BUILD_IMG2WEBP=OFF \
  -DWEBP_BUILD_VWEBP=OFF \
  -DWEBP_BUILD_WEBPINFO=OFF \
  -DWEBP_BUILD_WEBPMUX=OFF \
  -DWEBP_BUILD_EXTRAS=OFF

cmake --build "${libsharpyuv_build_dir}" --config Release --target sharpyuv

# Copy libsharpyuv.a to the expected location
cp -v "${libsharpyuv_build_dir}/Release-"*"/libsharpyuv.a" "${libsharpyuv_build_dir}/" || exit 255

cd ../..
# END libsharpyuv

# START avif
build_dir="_build-ios_${ARCH}"
rm -rf "${build_dir}"
mkdir -p "${build_dir}"

cmake -B "${build_dir}" -G Xcode \
  -DCMAKE_TOOLCHAIN_FILE="${IOS_TOOLCHAIN_FILE}" \
  -DPLATFORM="${BUILD_PLATFORM1}" \
  -DBUILD_SHARED_LIBS=OFF \
  -DAVIF_CODEC_DAV1D=ON \
  -DAVIF_LOCAL_DAV1D=ON \
  -DAVIF_LOCAL_LIBYUV=OFF \
  -DAVIF_LOCAL_LIBSHARPYUV=ON
cmake --build "${build_dir}" --config Release
# END avif

# START copy *.a & rm cache dir
mkdir -p "${IOS_OUTPUT_DIR}"

cp -v ext/dav1d/build/src/*.a "${IOS_OUTPUT_DIR}" || exit 255
cp -v ${build_dir}/Release-*/*.a "${IOS_OUTPUT_DIR}" || exit 255

rm -rf "ext/dav1d/build"
rm -rf "ext/libwebp/build"
rm -rf "${build_dir}"
# END copy *.a & rm cache dir
