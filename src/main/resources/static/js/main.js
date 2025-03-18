document.addEventListener('DOMContentLoaded', function () {
    let currentIndex = 0;
    const images = document.querySelectorAll('.slider-image');
    const dots = document.querySelectorAll('.slider-dot');
    const sliderContainer = document.querySelector('.slider'); // 배경색 변경 대상
    const totalImages = images.length;
    let autoSlideInterval;

    // ✅ 이미지별 배경색 설정
    const backgroundColors = [
        "#fcddd2", "#c8defa", "#f4d5c6",
        "#fde3e3", "#e0f4cb", "#fdd9ac"
    ];

    // ✅ 화살표 클릭 시 슬라이드 이동
    document.querySelector('.arrow-left').addEventListener('click', function () {
        stopAutoSlide(); // 자동 슬라이드 정지
        currentIndex = (currentIndex - 1 + totalImages) % totalImages;
        updateSlider();
        startAutoSlide(); // 자동 슬라이드 재시작
    });

    document.querySelector('.arrow-right').addEventListener('click', function () {
        stopAutoSlide();
        currentIndex = (currentIndex + 1) % totalImages;
        updateSlider();
        startAutoSlide();
    });

    // ✅ 점 클릭 시 해당 슬라이드로 이동
    dots.forEach((dot, index) => {
        dot.addEventListener('click', function () {
            stopAutoSlide();
            currentIndex = index;
            updateSlider();
            startAutoSlide();
        });
    });

    // ✅ 슬라이드 업데이트 함수
    function updateSlider() {
        images.forEach((image) => image.style.display = 'none');
        images[currentIndex].style.display = 'block';
        sliderContainer.style.backgroundColor = backgroundColors[currentIndex];
        updateDots();
    }

    // ✅ 점 상태 업데이트
    function updateDots() {
        dots.forEach((dot, index) => {
            dot.style.backgroundColor = index === currentIndex ? '#000' : '#fff';
        });
    }

    // ✅ 자동 슬라이드 함수 (3초마다 오른쪽 이동)
    function autoSlide() {
        currentIndex = (currentIndex + 1) % totalImages;
        updateSlider();
    }

    // ✅ 자동 슬라이드 시작
    function startAutoSlide() {
        autoSlideInterval = setInterval(autoSlide, 5000);
    }

    // ✅ 자동 슬라이드 정지
    function stopAutoSlide() {
        clearInterval(autoSlideInterval);
    }

    // ✅ 초기 상태 설정 및 자동 슬라이드 시작
    updateSlider();
    startAutoSlide();
});
