// 변수 선언
let regionData = {};

// 페이지 로드 시 지역 데이터 가져오기
async function fetchRegionData() {
    try {
        // 경로 수정: 슬래시로 시작하는 절대 경로 사용
        const response = await fetch('/static/sources/json/regionDate.json');
        if (!response.ok) {
            // 첫 번째 경로 실패시 대체 경로 시도
            console.warn('첫 번째 경로에서 지역 데이터를 불러오는데 실패했습니다. 대체 경로 시도...');
            const altResponse = await fetch('/sources/json/regionDate.json');
            if (!altResponse.ok) {
                throw new Error('지역 데이터를 불러오는데 실패했습니다. 상태: ' + altResponse.status);
            }
            regionData = await altResponse.json();
        } else {
            regionData = await response.json();
        }

        console.log('지역 데이터 로드 성공:', Object.keys(regionData).length + '개 지역 로드됨');

        // 시/도 옵션 초기화 및 추가
        const stateSelect = document.getElementById("region_state");
        // 기존 옵션 초기화 (첫 번째 옵션 제외)
        while (stateSelect.options.length > 1) {
            stateSelect.remove(1);
        }

        // 지역 데이터로부터 시/도 옵션 추가
        Object.keys(regionData).forEach(state => {
            const option = document.createElement("option");
            option.value = state;
            option.text = state;
            stateSelect.add(option);
        });

        // 초기 구/군 드롭다운 비활성화
        document.getElementById("region_district").disabled = true;
    } catch (error) {
        console.error('지역 데이터 로딩 오류:', error);

        // 오류 발생 시 하드코딩된 기본 데이터 사용
        console.warn('JSON 파일 로드 실패, 하드코딩된 지역 데이터 사용');
        regionData = defaultRegionData();

        // 시/도 옵션 초기화 및 추가
        const stateSelect = document.getElementById("region_state");
        // 기존 옵션 초기화 (첫 번째 옵션 제외)
        while (stateSelect.options.length > 1) {
            stateSelect.remove(1);
        }

        // 지역 데이터로부터 시/도 옵션 추가
        Object.keys(regionData).forEach(state => {
            const option = document.createElement("option");
            option.value = state;
            option.text = state;
            stateSelect.add(option);
        });
    }
}

// 기본 지역 데이터 (JSON 로드 실패 시 사용)
function defaultRegionData() {
    return {
        "서울특별시": ["전체", "강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구"],
        "부산광역시": ["전체", "강서구", "금정구", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사하구", "서구", "수영구", "연제구", "영도구", "중구", "해운대구", "기장군"],
        "대구광역시": ["전체", "남구", "달서구", "동구", "북구", "서구", "수성구", "중구", "달성군"],
        "인천광역시": ["전체", "계양구", "남구", "남동구", "동구", "부평구", "서구", "연수구", "중구", "강화군", "옹진군"],
        "광주광역시": ["전체", "광산구", "남구", "동구", "북구", "서구"],
        "대전광역시": ["전체", "대덕구", "동구", "서구", "유성구", "중구"],
        "울산광역시": ["전체", "남구", "동구", "북구", "중구", "울주군"],
        "경기도": ["전체", "고양시", "과천시", "광명시", "광주시", "구리시", "군포시", "김포시", "남양주시", "동두천시", "부천시", "성남시", "수원시", "시흥시", "안산시", "안성시", "안양시", "양주시", "오산시", "용인시", "의왕시", "의정부시", "이천시", "파주시", "평택시", "포천시", "하남시", "화성시", "가평군", "양평군", "연천군"]
    };
}

// 시/도 선택 시 구/군 업데이트
function getDistricts() {
    const stateSelect = document.getElementById("region_state");
    const districtSelect = document.getElementById("region_district");
    const loadingIndicator = document.getElementById("district-loading");
    const selectedState = stateSelect.value;

    // 시/도가 전국(ALL)이거나 없는 경우 구/군 비활성화 및 초기화
    if (selectedState === "ALL" || !selectedState) {
        districtSelect.disabled = true;
        while (districtSelect.options.length > 0) {
            districtSelect.remove(0);
        }
        const option = document.createElement("option");
        option.value = "";
        option.text = "선택 안함";
        districtSelect.add(option);

        // 전체 지역값 업데이트
        document.getElementById("club_region").value = "ALL";
        return;
    }

    // 구/군 활성화
    districtSelect.disabled = false;

    // 로딩 표시 보이기
    loadingIndicator.style.display = "block";

    try {
        // 구/군 옵션 초기화
        while (districtSelect.options.length > 0) {
            districtSelect.remove(0);
        }

        // 선택 안함 옵션 추가
        const emptyOption = document.createElement("option");
        emptyOption.value = "";
        emptyOption.text = "선택 안함";
        districtSelect.add(emptyOption);

        // 선택된 시/도에 해당하는 구/군 옵션 추가
        if (regionData[selectedState]) {
            regionData[selectedState].forEach(district => {
                const option = document.createElement("option");
                option.value = district;
                option.text = district;
                districtSelect.add(option);
            });
        }

        // 시/도만 선택된 경우 지역값 업데이트
        updateRegionValue();
    } catch (error) {
        console.error("구/군 목록 업데이트 중 오류 발생:", error);
    } finally {
        // 로딩 표시 숨기기
        loadingIndicator.style.display = "none";
    }
}

// 지역 선택값 업데이트
function updateRegionValue() {
    const stateSelect = document.getElementById("region_state");
    const districtSelect = document.getElementById("region_district");
    const regionInput = document.getElementById("club_region");

    // 선택된 시/도와 구/군 가져오기
    const state = stateSelect.value;
    const district = districtSelect.value;

    // 전국(ALL)이 선택된 경우
    if (state === "ALL" || !state) {
        regionInput.value = "ALL";
        return;
    }

    // 시/도만 선택된 경우 (구/군 선택 안함)
    if (!district || district === "" || district === "전체") {
        regionInput.value = state;
        return;
    }

    // 시/도와 구/군 모두 선택된 경우
    regionInput.value = state + district;
}

// 대분류 선택 시 소분류 가져오기 함수
function getSubCategories() {
    const categoryType = document.getElementById("category_type").value;
    const subCategorySelect = document.getElementById("club_category");
    const loadingIndicator = document.getElementById("sub-category-loading");

    if (!categoryType) {
        // 대분류가 선택되지 않았을 경우 소분류 초기화
        while (subCategorySelect.options.length > 1) {
            subCategorySelect.remove(1);
        }
        return;
    }

    // 로딩 표시 보이기
    loadingIndicator.style.display = "block";

    // AJAX 요청
    const xhr = new XMLHttpRequest();
    xhr.open("GET", "/club/get_sub_categories?category_type=" + encodeURIComponent(categoryType), true);

    xhr.onload = function() {
        // 로딩 표시 숨기기
        loadingIndicator.style.display = "none";

        if (xhr.status === 200) {
            const categories = JSON.parse(xhr.responseText);

            // 기존 옵션 초기화 (첫 번째 옵션 제외)
            while (subCategorySelect.options.length > 1) {
                subCategorySelect.remove(1);
            }

            // 새 옵션 추가
            categories.forEach(function(category) {
                const option = document.createElement("option");
                option.value = category.category_name;
                option.text = category.category_name;
                subCategorySelect.add(option);
            });
        } else {
            console.error("카테고리 가져오기 실패: " + xhr.status);
        }
    };

    xhr.onerror = function() {
        // 로딩 표시 숨기기
        loadingIndicator.style.display = "none";
        console.error("카테고리 가져오기 오류 발생");
    };

    xhr.send();
}

// 출생년도로부터 나이 계산하여 업데이트하는 함수
function updateAgeFromYear(birthYear) {
    const currentYear = new Date().getFullYear();
    let age = 0;

    if (birthYear > 0) {
        age = currentYear - birthYear;
    }

    document.getElementById('age-value').textContent = age;
    document.getElementById('club_agemin').value = age;
}

// 폼 제출 전 검증
function validateForm(event) {
    // club_region 값 확인 및 설정
    const regionInput = document.getElementById('club_region');
    if (!regionInput.value || regionInput.value.trim() === '') {
        // 값이 없으면 ALL로 설정
        regionInput.value = "ALL";
    }
    console.log('제출 시 지역 값:', regionInput.value);

    // 나머지 폼 검증 로직은 여기에 추가
    return true;
}

// 이미지 미리보기 설정
function setupImagePreview() {
    const uploadInput = document.getElementById('clubImageUpload');
    if (!uploadInput) {
        console.error('clubImageUpload 요소를 찾을 수 없습니다.');
        return;
    }

    uploadInput.addEventListener('change', function(event) {
        const preview = document.getElementById('profilePreview');
        const file = event.target.files[0];
        const fileWarning = document.getElementById('fileWarning');

        if (file) {
            if (file.size > 5 * 1024 * 1024) {
                fileWarning.style.display = "block";
                return;
            } else {
                fileWarning.style.display = "none";
            }

            const reader = new FileReader();
            reader.onload = function(e) {
                preview.src = e.target.result;
                preview.style.display = "block";
            };
            reader.readAsDataURL(file);
        }
    });
}

// 년도 옵션 설정
function setupYearOptions() {
    const birthYearSelect = document.getElementById('birth_year');
    if (!birthYearSelect) {
        console.error('birth_year 요소를 찾을 수 없습니다.');
        return;
    }

    const currentYear = new Date().getFullYear();

    // 년도 옵션 생성 (현재 년도부터 100년 전까지)
    for (let year = currentYear; year >= currentYear - 100; year--) {
        const option = document.createElement('option');
        option.value = year;
        option.textContent = year + '년';
        birthYearSelect.appendChild(option);
    }

    // 초기값 설정
    const clubAgeMin = document.getElementById('club_agemin').value;
    if (clubAgeMin > 0) {
        const birthYear = currentYear - clubAgeMin;
        birthYearSelect.value = birthYear;
        updateAgeFromYear(birthYear);
    }
}

// 저장된 지역 값 로드
function loadSavedRegion() {
    const savedRegion = document.getElementById('club_region').value;
    console.log('저장된 지역 값:', savedRegion);

    if (savedRegion && savedRegion !== "ALL" && Object.keys(regionData).length > 0) {
        // 시/도와 구/군 분리
        const parts = savedRegion.split(" ");
        const state = parts[0];
        const district = parts.length > 1 ? parts[1] : "";

        // 시/도 설정
        if (Object.keys(regionData).includes(state)) {
            document.getElementById("region_state").value = state;
            getDistricts();

            // 구/군 설정 (있는 경우)
            setTimeout(() => {
                if (district && document.getElementById("region_district").options) {
                    document.getElementById("region_district").value = district;
                }
            }, 100); // 약간의 지연을 두어 구/군 옵션이 로드된 후 설정
        }
    }
}

// 페이지 초기화
document.addEventListener('DOMContentLoaded', async function() {
    console.log('페이지 로드됨: 지역 데이터 가져오기 시도...');

    try {
        // 필수 요소 확인
        const clubRegion = document.getElementById('club_region');
        if (!clubRegion) {
            console.error('club_region 요소를 찾을 수 없습니다.');
            return;
        }

        // 기본값 설정 (페이지 로드 시 항상 ALL로 초기화)
        clubRegion.value = "ALL";

        // 지역 데이터 가져오기
        await fetchRegionData();

        // 각 기능 설정
        setupImagePreview();
        setupYearOptions();
        loadSavedRegion();

        // 이벤트 리스너 설정
        const stateSelect = document.getElementById('region_state');
        if (stateSelect) {
            stateSelect.addEventListener('change', getDistricts);
        }

        const districtSelect = document.getElementById('region_district');
        if (districtSelect) {
            districtSelect.addEventListener('change', updateRegionValue);
        }

        const categoryType = document.getElementById('category_type');
        if (categoryType) {
            categoryType.addEventListener('change', getSubCategories);
        }

        const birthYear = document.getElementById('birth_year');
        if (birthYear) {
            birthYear.addEventListener('change', function() {
                updateAgeFromYear(this.value);
            });
        }

    } catch (error) {
        console.error('초기화 중 오류 발생:', error);
    }
});