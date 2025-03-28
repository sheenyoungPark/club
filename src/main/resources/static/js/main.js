document.addEventListener('DOMContentLoaded', function () {
    let currentIndex = 0;
    const images = document.querySelectorAll('.slider-image');
    const dots = document.querySelectorAll('.slider-dot');
    const sliderContainer = document.querySelector('.slider'); // 배경색 변경 대상
    const totalImages = images.length;
    const fadeElements = document.querySelectorAll('.fade-in-section');
    let autoSlideInterval;

    // 이미지별 배경색 설정
    const backgroundColors = [
        "#fcddd2", "#c8defa", "#f4d5c6",
        "#fde3e3", "#e0f4cb"
    ];

    // 화살표 클릭 시 슬라이드 이동
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

    // 점 클릭 시 해당 슬라이드로 이동
    dots.forEach((dot, index) => {
        dot.addEventListener('click', function () {
            stopAutoSlide();
            currentIndex = index;
            updateSlider();
            startAutoSlide();
        });
    });

    // 슬라이드 업데이트 함수
    function updateSlider() {
        // 모든 이미지 숨기기
        images.forEach((image) => {
            image.style.display = 'none';
        });

        // 현재 이미지만 표시
        images[currentIndex].style.display = 'block';

        // 배경색 변경
        sliderContainer.style.backgroundColor = backgroundColors[currentIndex];

        // 점 상태 업데이트
        updateDots();
    }

    // 점 상태 업데이트
    function updateDots() {
        dots.forEach((dot, index) => {
            if (index === currentIndex) {
                dot.classList.add('active');
                dot.style.backgroundColor = '#000';
            } else {
                dot.classList.remove('active');
                dot.style.backgroundColor = 'rgba(255, 255, 255, 0.5)';
            }
        });
    }

    // 자동 슬라이드 함수 (5초마다 오른쪽 이동)
    function autoSlide() {
        currentIndex = (currentIndex + 1) % totalImages;
        updateSlider();
    }

    // 자동 슬라이드 시작
    function startAutoSlide() {
        stopAutoSlide(); // 기존 타이머 제거
        autoSlideInterval = setInterval(autoSlide, 5000);
    }

    // 자동 슬라이드 정지
    function stopAutoSlide() {
        clearInterval(autoSlideInterval);
    }

    // 이미지 크기 조정 함수
    function adjustImageSizes() {
        // 슬라이더 이미지 크기 조정
        const sliderImages = document.querySelectorAll('.slider-image img');
        sliderImages.forEach(img => {
            img.style.objectFit = 'cover';
            img.style.width = '100%';
            img.style.height = '100%';
        });

        // 추천 상품 이미지 크기 조정
        const itemImages = document.querySelectorAll('.item-image');
        itemImages.forEach(img => {
            img.style.objectFit = 'cover';
            img.style.width = '100%';
            img.style.height = '100%';
        });
    }

    // 초기 상태 설정 및 자동 슬라이드 시작
    function initSlider() {
        // 초기에 첫 번째 이미지를 보여줌
        if (images.length > 0) {
            // 모든 이미지 숨기기
            images.forEach((image) => {
                image.style.display = 'none';
            });

            // 첫 번째 이미지만 표시
            images[0].style.display = 'block';

            // 첫 번째 점 활성화
            if (dots.length > 0) {
                dots[0].classList.add('active');
                dots[0].style.backgroundColor = '#000';
            }
        }

        // 슬라이더 배경색 설정
        if (backgroundColors.length > 0) {
            sliderContainer.style.backgroundColor = backgroundColors[0];
        }

        // 자동 슬라이드 시작
        startAutoSlide();
    }

    // 초기화 함수 실행
    initSlider();
    adjustImageSizes();

    // 페이지 로드 시 상단 섹션 바로 표시 (스크롤 없이)
    const topSection = document.querySelector('.top-section-container');
    if (topSection) {
        // 약간의 지연 후 표시 (페이지 로드 애니메이션)
        setTimeout(() => {
            topSection.classList.add('is-visible');
        }, 100);
    }

    // 카테고리 및 아이템, 클럽 박스에 인덱스 설정 (순차적 애니메이션용)
    const itemBoxes = document.querySelectorAll('.item-box');
    itemBoxes.forEach((box, index) => {
        box.style.setProperty('--item-index', index);
    });

    const categories = document.querySelectorAll('.category');
    categories.forEach((category, index) => {
        category.style.setProperty('--item-index', index);
    });

    const clubBoxes = document.querySelectorAll('.club-box');
    clubBoxes.forEach((box, index) => {
        box.style.setProperty('--item-index', index);
    });

    // Intersection Observer 생성
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            // 요소가 화면에 보이면 is-visible 클래스 추가
            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible');
                // 애니메이션이 완료된 후에는 더이상 관찰하지 않음
                observer.unobserve(entry.target);
            }
        });
    }, {
        // 요소가 20% 이상 보일 때 콜백 실행
        threshold: 0.2,
        // 뷰포트 기준 (기본값)
        rootMargin: '0px'
    });

    // 모든 fade-in-section 요소를 관찰 대상으로 등록
    fadeElements.forEach(elem => {
        // 상단 섹션이 아닌 경우에만 observer에 등록 (상단 섹션은 이미 처리됨)
        if (elem !== topSection) {
            observer.observe(elem);
        }
    });

    // 창 크기가 변경될 때 이미지 크기 조정
    window.addEventListener('resize', adjustImageSizes);
});