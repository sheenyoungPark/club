document.addEventListener('DOMContentLoaded', function () {
    let currentIndex = 0;
    const images = document.querySelectorAll('.slider-image');
    const dots = document.querySelectorAll('.slider-dot');
    const sliderContainer = document.querySelector('.slider'); // 배경색 변경 대상
    const sliderImages = document.querySelector('.slider-images');
    const totalImages = images.length;

    // 이미지별 배경색 설정 (원하는 RGB 값 입력)
    const backgroundColors = [
        "#fcddd2", // 첫 번째 이미지 배경색
        "#c8defa", // 두 번째 이미지 배경색
        "#f4d5c6", // 세 번째 이미지 배경색
        "#fde3e3", // 네 번째 이미지 배경색
        "#e0f4cb", // 다섯 번째 이미지 배경색
        "#fdd9ac"  // 여섯 번째 이미지 배경색
    ];

    // 화살표 클릭 시 슬라이드 이동
    document.querySelector('.arrow-left').addEventListener('click', function () {
        currentIndex = (currentIndex - 1 + totalImages) % totalImages;
        updateSlider();
    });

    document.querySelector('.arrow-right').addEventListener('click', function () {
        currentIndex = (currentIndex + 1) % totalImages;
        updateSlider();
    });

    // 점 클릭 시 해당 슬라이드로 이동
    dots.forEach((dot, index) => {
        dot.addEventListener('click', function () {
            currentIndex = index;
            updateSlider();
        });
    });

    // 슬라이드 업데이트 함수
    function updateSlider() {
        // 모든 슬라이드 이미지를 숨기기
        images.forEach((image) => {
            image.style.display = 'none';
        });

        // 현재 인덱스에 해당하는 이미지를 보이게 하기
        images[currentIndex].style.display = 'block';

        // 슬라이더 배경색 변경
        sliderContainer.style.backgroundColor = backgroundColors[currentIndex];

        // 점 상태 업데이트
        updateDots();
    }

    // 점 상태 업데이트
    function updateDots() {
        dots.forEach((dot, index) => {
            dot.style.backgroundColor = index === currentIndex ? '#000' : '#fff';
        });
    }

    // 초기 상태 설정
    updateSlider();
});