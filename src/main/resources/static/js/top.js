// 쿠키 관리 함수
function setCookie(name, value, days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    const expires = "expires=" + date.toUTCString();
    document.cookie = name + "=" + value + ";" + expires + ";path=/";
}

function getCookie(name) {
    const cookieName = name + "=";
    const cookies = document.cookie.split(';');
    for(let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i].trim();
        if (cookie.indexOf(cookieName) === 0) {
            return cookie.substring(cookieName.length, cookie.length);
        }
    }
    return "";
}

// 최근 검색어 관리 함수
function addSearchTerm(term) {
    if (!term) return; // 빈 검색어는 저장하지 않음

    let searchHistory = getSearchHistory();

    // 중복 검색어 제거
    searchHistory = searchHistory.filter(item => item !== term);

    // 최신 검색어를 배열 앞에 추가
    searchHistory.unshift(term);

    // 최대 10개까지만 저장
    if (searchHistory.length > 10) {
        searchHistory.pop();
    }

    // 쿠키에 저장 (7일간 유지)
    setCookie('searchHistory', JSON.stringify(searchHistory), 7);

    // 화면 갱신
    displaySearchHistory();
}

function getSearchHistory() {
    const history = getCookie('searchHistory');
    return history ? JSON.parse(history) : [];
}

function displaySearchHistory() {
    const searchHistory = getSearchHistory();
    const searchTagsContainer = document.getElementById('searchTagsContainer');

    // 기존 태그들 제거
    if (searchTagsContainer) {
        searchTagsContainer.innerHTML = '';

        // 최근 검색어 태그 추가
        searchHistory.forEach(term => {
            const tagDiv = document.createElement('div');
            tagDiv.className = 'tag';
            tagDiv.textContent = term;

            // 태그 클릭 시 검색어 자동 입력
            tagDiv.addEventListener('click', () => {
                const searchInput = document.querySelector('input[name="searchtxt"]');
                if (searchInput) {
                    searchInput.value = term;
                }
            });

            searchTagsContainer.appendChild(tagDiv);
        });
    }
}

// 지역 데이터 초기화 함수
function initializeRegionData() {
    // 서버에서 전달한 locations 데이터를 regionData 형식으로 변환
    let regionData = {};

    // 서버에서 전달한 locations 데이터 처리
    if (window.locationsFromServer && Array.isArray(window.locationsFromServer)) {
        // locations 데이터를 regionData 형식으로 변환
        window.locationsFromServer.forEach(location => {
            if (!regionData[location.city]) {
                regionData[location.city] = [];
            }
            if (regionData[location.city].indexOf(location.district) === -1) {
                regionData[location.city].push(location.district);
            }
        });
    } else {
        // 기본 데이터(하드코딩) 사용
        regionData = {
            "서울특별시": ["강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구"],
            "부산광역시": ["강서구", "금정구", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사하구", "서구"],
            "인천광역시": ["계양구", "남구", "남동구", "동구", "미추홀구", "부평구", "서구", "연수구", "중구"],
            "대구광역시": ["남구", "달서구", "동구", "북구", "서구", "수성구", "중구"],
            "대전광역시": ["대덕구", "동구", "서구", "유성구", "중구"]
        };
    }

    return regionData;
}

document.addEventListener('DOMContentLoaded', function() {
    // 요소 선택
    let searchContainer = document.getElementById('searchContainer');
    let searchToggle = document.getElementById('searchToggle');
    const regionDropdown = document.getElementById('regionDropdown');
    const regionModal = document.getElementById('regionModal');
    const regionModalClose = document.getElementById('regionModalClose');
    const confirmRegion = document.getElementById('confirmRegion');
    const cityList = document.getElementById('cityList');
    const districtList = document.getElementById('districtList');

    // 검색 폼 선택 및 이벤트 연결
    const searchForm = document.querySelector('.search-box form');

    // 페이지 로드 시 저장된 검색어 표시
    displaySearchHistory();

    // 폼 제출 시 중복 region 매개변수 제거
    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            // 먼저 기존 이벤트 실행 중지
            e.preventDefault();

            // 검색어 저장 로직
            const searchInput = document.querySelector('input[name="searchtxt"]');
            if (searchInput) {
                const searchTerm = searchInput.value.trim();
                if (searchTerm) {
                    addSearchTerm(searchTerm);
                }
            }

            // 폼 데이터 수집
            const formData = new FormData(searchForm);

            // URL 쿼리 문자열 생성
            const params = new URLSearchParams();

            // 중복 없이 폼 데이터 추가
            // region은 한 번만 추가되도록 함
            let regionAdded = false;

            for (const [key, value] of formData.entries()) {
                // region 매개변수인 경우 중복 검사
                if (key === 'region') {
                    if (!regionAdded) {
                        params.append(key, value);
                        regionAdded = true;
                    }
                } else {
                    // 다른 매개변수는 그대로 추가
                    params.append(key, value);
                }
            }

            // region 매개변수가 없으면 빈 값으로 추가
            if (!regionAdded) {
                params.append('region', '');
            }

            // 새 URL 생성 및 이동
            const url = `${searchForm.action}?${params.toString()}`;
            window.location.href = url;
        });
    }

    // 검색 토글 버튼 이벤트
    if (searchToggle && searchContainer) {
        searchToggle.addEventListener('click', function(e) {
            e.preventDefault(); // 기본 동작 방지
            e.stopPropagation(); // 이벤트 버블링 방지

            // 직접 style 속성으로 표시/숨김 처리
            if (searchContainer.style.display === 'none' || getComputedStyle(searchContainer).display === 'none') {
                searchContainer.style.display = 'block';
                searchContainer.classList.remove('hidden');
            } else {
                searchContainer.style.display = 'none';
                searchContainer.classList.add('hidden');
            }
        });
    }

    // 변수 초기화
    let selectedCity = null;
    let selectedDistrict = null;

    // 지역 데이터 초기화
    let regionData = initializeRegionData();

    // 지역 드롭다운 초기값 설정
    if (regionDropdown) {
        // 페이지 로드 시 select 요소 내의 첫 번째 option에 value="" 설정
        const firstOption = regionDropdown.querySelector('option');
        if (firstOption) {
            firstOption.value = '';
        }
    }

    // 시/도 목록 렌더링 함수
    function renderCityList() {
        if (!cityList) return;

        cityList.innerHTML = '';
        Object.keys(regionData).forEach(city => {
            const div = document.createElement('div');
            div.className = 'city-item';
            div.textContent = city;

            div.addEventListener('click', () => {
                document.querySelectorAll('.city-item').forEach(item => item.classList.remove('active'));
                div.classList.add('active');
                selectedCity = city;
                renderDistrictList(city);
            });

            cityList.appendChild(div);
        });
    }

    // 군/구 목록 렌더링 함수
    function renderDistrictList(city) {
        if (!districtList) return;

        districtList.innerHTML = '';
        if (regionData[city]) {
            // 구/군 목록 복사 및 정렬 (가나다 순)
            const districts = [...regionData[city]];
            // '전체' 옵션이 있으면 정렬에서 제외
            const hasAllOption = districts.includes('전체');

            if (hasAllOption) {
                // '전체'를 제외한 나머지 항목을 정렬
                const allOption = districts.splice(districts.indexOf('전체'), 1)[0];
                districts.sort();
                // 정렬 후 '전체'를 다시 배열의 맨 앞에 추가
                districts.unshift(allOption);
            } else {
                // '전체' 옵션이 없으면 모든 항목 정렬
                districts.sort();
            }

            // '전체' 옵션이 있는 경우 구/군 목록 상단에 먼저 추가
            if (hasAllOption) {
                const allDiv = document.createElement('div');
                allDiv.className = 'district-item all-district';
                allDiv.textContent = '전체';
                allDiv.style.textAlign = 'center';
                allDiv.style.width = 'calc(100% - 10px)'
                allDiv.style.fontWeight = 'bold';
                allDiv.style.backgroundColor = '#e9e5f3';
                allDiv.style.marginBottom = '10px';

                allDiv.addEventListener('click', () => {
                    document.querySelectorAll('.district-item').forEach(item => {
                        item.style.backgroundColor = '#f0f0f7';
                        item.style.color = 'black';
                    });
                    allDiv.style.backgroundColor = '#5d4b8c';
                    allDiv.style.color = 'white';
                    selectedDistrict = '전체';
                });

                districtList.appendChild(allDiv);
            }

            // 나머지 구/군 목록 추가 ('전체' 제외)
            districts.forEach(district => {
                if (district !== '전체') {  // '전체'는 이미 추가했으므로 제외
                    const div = document.createElement('div');
                    div.className = 'district-item';
                    div.textContent = district;

                    div.addEventListener('click', () => {
                        document.querySelectorAll('.district-item').forEach(item => {
                            item.style.backgroundColor = '#f0f0f7';
                            item.style.color = 'black';
                        });
                        div.style.backgroundColor = '#5d4b8c';
                        div.style.color = 'white';
                        selectedDistrict = district;
                    });

                    districtList.appendChild(div);
                }
            });
        }
    }

    // 지역 드롭다운 클릭 이벤트
    if (regionDropdown) {
        regionDropdown.addEventListener('click', function(e) {
            e.stopPropagation();
            e.preventDefault();
            // 시/도와 구/군 모두 초기화
            selectedCity = null;
            selectedDistrict = null;

            // 선택된 시/도 항목의 active 클래스 제거
            document.querySelectorAll('.city-item').forEach(item => {
                item.classList.remove('active');
            });

            // 구/군 목록 비우기
            if (districtList) districtList.innerHTML = '';

            // 모달 표시
            if (regionModal) {
                regionModal.style.display = 'block';
                // 시/도 목록 렌더링
                renderCityList();
            }
        });
    }

    // 모달 닫기 및 선택 반영 함수
    function closeModalWithReset() {
        let selectedRegionText = '지역';

        // 시/도와 구/군이 모두 선택된 경우
        if (selectedCity && selectedDistrict) {
            selectedRegionText = selectedCity + " " + selectedDistrict;
        }
        // 시/도만 선택된 경우
        else if (selectedCity) {
            selectedRegionText = '지역';
        }

        if (regionDropdown) {
            regionDropdown.innerHTML = '';
            const newOption = document.createElement('option');
            newOption.selected = true;
            newOption.textContent = selectedRegionText;

            // value 값도 적절히 설정 (폼 제출 시 사용)
            if (selectedCity && selectedDistrict) {
                newOption.value = selectedCity + selectedDistrict;
            } else if (selectedCity) {
                newOption.value = '';
            } else {
                newOption.value = "";
            }

            regionDropdown.appendChild(newOption);
        }

        if (regionModal) {
            regionModal.style.display = 'none';
        }
    }

    // 모달 닫기 버튼 이벤트
    if (regionModalClose) {
        regionModalClose.addEventListener('click', function(e) {
            e.preventDefault();
            closeModalWithReset();
        });
    }

    // 확인 버튼 이벤트
    if (confirmRegion) {
        confirmRegion.addEventListener('click', function(e) {
            e.preventDefault();
            closeModalWithReset();
        });
    }

    // 모달 외부 클릭 이벤트
    document.addEventListener('click', function(e) {
        if (regionModal && regionModal.style.display === 'block' && !regionModal.contains(e.target) && e.target !== regionDropdown) {
            closeModalWithReset();
        }
    });

    // '전체' 옵션 추가 (필요한 경우)
    Object.keys(regionData).forEach(city => {
        if (regionData[city] && !regionData[city].includes('전체')) {
            // 이미 '전체'가 포함되어 있지 않은 경우에만 추가
            regionData[city].unshift('전체');
        }
    });
});