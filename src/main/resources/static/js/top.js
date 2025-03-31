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
    let selectedCity = null;
    let selectedDistrict = null;
    let regionData = initializeRegionData();

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
                    allDiv.style.backgroundColor = '#9cee69'; // #5d4b8c에서 #9cee69로 변경
                    allDiv.style.color = 'black'; // 텍스트 색상을 검정색으로 변경
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
                        div.style.backgroundColor = '#9cee69'; // #5d4b8c에서 #9cee69로 변경
                        div.style.color = 'black'; // 텍스트 색상을 검정색으로 변경
                        selectedDistrict = district;
                    });

                    districtList.appendChild(div);
                }
            });
        }
    }

    // 로고 링크 요소 가져오기
    const logoLink = document.querySelector('.header a:first-child');

    if (logoLink) {
        // 호버 텍스트 요소 생성
        const hoverText = document.createElement('div');
        hoverText.className = 'logo-hover-text';

        // 각 줄을 분리해서 처리
        const lines = ['우리', '주변의', '동호회'];

        lines.forEach(line => {
            const lineDiv = document.createElement('div');

            // 첫 글자 분리해서 색상 변경
            const firstLetter = line.charAt(0);
            const restOfLine = line.substring(1);

            // 첫 글자 요소 생성
            const firstLetterSpan = document.createElement('span');
            firstLetterSpan.className = 'first-letter';
            firstLetterSpan.textContent = firstLetter;

            // 나머지 텍스트 생성
            const restSpan = document.createElement('span');
            restSpan.textContent = restOfLine;

            // 줄에 첫 글자와 나머지 텍스트 추가
            lineDiv.appendChild(firstLetterSpan);
            lineDiv.appendChild(restSpan);

            // 생성된 줄을 호버 텍스트에 추가
            hoverText.appendChild(lineDiv);
        });

        // 로고 컨테이너에 추가 (display 속성 제거)
        logoLink.appendChild(hoverText);

        // 호버 이벤트는 CSS에서 처리하므로 JavaScript 이벤트 리스너 제거
        // mouseenter와 mouseleave 이벤트 리스너 제거
    }



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

    // 검색 토글 버튼 이벤트 개선
    if (searchToggle && searchContainer) {
        // 초기 상태 설정
        searchContainer.classList.add('hidden');

        // 검색창 열고 닫는 함수
        function toggleSearchContainer() {
            const isHidden = searchContainer.classList.contains('hidden');

            if (isHidden) {
                // 검색창 열기
                searchContainer.classList.remove('hidden');
                searchContainer.style.animation = 'searchAppear 0.3s cubic-bezier(0.4, 0, 0.2, 1) forwards';
            } else {
                // 검색창 닫기
                // 애니메이션 효과를 위해 바로 hidden 클래스를 추가하지 않고 애니메이션 후 추가
                searchContainer.style.animation = 'searchDisappear 0.3s cubic-bezier(0.4, 0, 0.2, 1) forwards';

                // 애니메이션이 끝난 후 hidden 클래스 추가
                setTimeout(() => {
                    searchContainer.classList.add('hidden');
                }, 300); // 애니메이션 시간과 동일하게 설정 (0.3초)
            }
        }

        // 검색 토글 버튼 클릭 이벤트 리스너
        searchToggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            toggleSearchContainer();
        });
    }

    // '전체' 옵션 추가 (필요한 경우)
    Object.keys(regionData).forEach(city => {
        if (regionData[city] && !regionData[city].includes('전체')) {
            // 이미 '전체'가 포함되어 있지 않은 경우에만 추가
            regionData[city].unshift('전체');
        }
    });

    // 기존 셀렉트 박스를 버튼으로 교체
    if (regionDropdown && regionDropdown.parentNode) {
        // 새 버튼 생성
        const regionButton = document.createElement('button');
        regionButton.id = 'regionButton';
        regionButton.className = 'region-button';
        regionButton.innerHTML = '<span>지역</span> <i class="fas fa-chevron-down"></i>';

        // 버튼 스타일 적용
        regionButton.style.padding = '12px 15px';
        regionButton.style.borderRadius = '5px';
        regionButton.style.border = '1px solid #e0e0e0';
        regionButton.style.backgroundColor = 'white';
        regionButton.style.fontSize = '15px';
        regionButton.style.minWidth = '150px';
        regionButton.style.cursor = 'pointer';
        regionButton.style.display = 'flex';
        regionButton.style.alignItems = 'center';
        regionButton.style.justifyContent = 'space-between';
        regionButton.style.transition = 'border-color 0.3s';

        // 아이콘 스타일
        const icon = regionButton.querySelector('i');
        if (icon) {
            icon.style.marginLeft = '10px';
        }

        // 드롭다운을 버튼으로 교체
        regionDropdown.parentNode.replaceChild(regionButton, regionDropdown);

        // 버튼 클릭 이벤트 처리 - 토글 기능 추가
        regionButton.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            // 이미 모달이 표시되어 있으면 닫기
            if (regionModal.style.display === 'block') {
                regionModal.style.display = 'none';
                return;
            }

            // 모달 위치 조정 - 버튼 바로 아래에 위치하도록 수정
            const buttonRect = regionButton.getBoundingClientRect();

            // 위치를 absolute로 변경하고 부모 요소에 대한 상대적 위치 설정
            regionModal.style.position = 'absolute';
            regionModal.style.top = buttonRect.height + 'px';
            regionModal.style.left = '0px';

            // 모달이 화면 밖으로 나가지 않도록 조정
            const modalWidth = 550; // 모달 너비
            if (buttonRect.left + modalWidth > window.innerWidth) {
                // 오른쪽 화면 경계를 넘어가면 오른쪽 정렬
                regionModal.style.left = 'auto';
                regionModal.style.right = '0px';
            }

            // 모달 표시
            regionModal.style.display = 'block';

            // 나머지 초기화 코드는 그대로 유지
            selectedCity = null;
            selectedDistrict = null;
            document.querySelectorAll('.city-item').forEach(item => {
                item.classList.remove('active');
            });
            if (districtList) districtList.innerHTML = '';
            renderCityList();
        });

        // 모달 닫기 및 선택 반영 함수
        function closeModalWithSelection() {
            let selectedRegionText = '지역';

            // 시/도와 구/군이 모두 선택된 경우
            if (selectedCity && selectedDistrict) {
                selectedRegionText = selectedCity + " " + selectedDistrict;
            }
            // 시/도만 선택된 경우
            else if (selectedCity) {
                selectedRegionText = selectedCity;
            }

            // 버튼 텍스트 업데이트
            regionButton.innerHTML = '<span>' + selectedRegionText + '</span> <i class="fas fa-chevron-down"></i>';

            // 모달 닫기
            if (regionModal) {
                regionModal.style.display = 'none';
            }

            // 선택된 지역 값을 form에 저장하기 위한 hidden input 추가
            let hiddenInput = document.querySelector('input[name="region"]');
            if (!hiddenInput) {
                hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'region';
                regionButton.parentNode.appendChild(hiddenInput);
            }

            // 값 설정
            if (selectedCity && selectedDistrict) {
                hiddenInput.value = selectedCity + selectedDistrict;
            } else {
                hiddenInput.value = '';
            }
        }

        // 모달 닫기 버튼 이벤트
        if (regionModalClose) {
            regionModalClose.addEventListener('click', function(e) {
                e.preventDefault();
                closeModalWithSelection();
            });
        }

        // 확인 버튼 이벤트
        if (confirmRegion) {
            confirmRegion.addEventListener('click', function(e) {
                e.preventDefault();
                closeModalWithSelection();
            });
        }

        // 모달 외부 클릭 이벤트
        document.addEventListener('click', function(e) {
            if (regionModal && regionModal.style.display === 'block' &&
                !regionModal.contains(e.target) && e.target !== regionButton) {
                closeModalWithSelection();
            }
        });
    }
});

document.addEventListener('DOMContentLoaded', function() {
    // 기존 코드는 유지하고, 햄버거 메뉴 관련 코드를 추가합니다

    // 햄버거 메뉴 요소 선택
    const hamburgerMenu = document.getElementById('hamburgerMenu');
    const mobileMenuContainer = document.getElementById('mobileMenuContainer');
    const mobileMenuOverlay = document.getElementById('mobileMenuOverlay');

    // 햄버거 메뉴 토글 함수
    function toggleMobileMenu() {
        hamburgerMenu.classList.toggle('active');
        mobileMenuContainer.classList.toggle('active');
        mobileMenuOverlay.classList.toggle('active');

        // 스크롤 막기/허용
        if (mobileMenuContainer.classList.contains('active')) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = '';
        }
    }

    // 햄버거 메뉴 클릭 이벤트
    if (hamburgerMenu) {
        hamburgerMenu.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleMobileMenu();
        });
    }

    // 오버레이 클릭 시 메뉴 닫기
    if (mobileMenuOverlay) {
        mobileMenuOverlay.addEventListener('click', function() {
            toggleMobileMenu();
        });
    }

    // 모바일 메뉴 내부 링크 클릭 시 메뉴 닫기
    if (mobileMenuContainer) {
        const mobileMenuLinks = mobileMenuContainer.querySelectorAll('a');
        mobileMenuLinks.forEach(link => {
            link.addEventListener('click', function() {
                toggleMobileMenu();
            });
        });
    }

    // 창 크기 변경 시 모바일 메뉴 상태 관리
    window.addEventListener('resize', function() {
        if (window.innerWidth > 768) {
            // 모바일 메뉴가 열려있다면 닫기
            if (mobileMenuContainer && mobileMenuContainer.classList.contains('active')) {
                hamburgerMenu.classList.remove('active');
                mobileMenuContainer.classList.remove('active');
                mobileMenuOverlay.classList.remove('active');
                document.body.style.overflow = '';
            }
        }
    });

    // 페이지 스크롤 시 모바일 메뉴 닫기
    window.addEventListener('scroll', function() {
        if (mobileMenuContainer && mobileMenuContainer.classList.contains('active')) {
            hamburgerMenu.classList.remove('active');
            mobileMenuContainer.classList.remove('active');
            mobileMenuOverlay.classList.remove('active');
            document.body.style.overflow = '';
        }
    });

    // 검색 컨테이너와 모바일 메뉴 상호작용
    const searchToggle = document.getElementById('searchToggle');
    const searchContainer = document.getElementById('searchContainer');

    if (searchToggle && searchContainer) {
        searchToggle.addEventListener('click', function() {
            // 모바일 메뉴가 열려있다면 닫기
            if (mobileMenuContainer && mobileMenuContainer.classList.contains('active')) {
                hamburgerMenu.classList.remove('active');
                mobileMenuContainer.classList.remove('active');
                mobileMenuOverlay.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
    }

    // 모바일 메뉴에서 unreadChatCount 동기화
    function syncUnreadChatCount() {
        const originalBadge = document.getElementById('unreadChatCount');
        const mobileBadge = document.getElementById('mobileUnreadChatCount');

        if (originalBadge && mobileBadge) {
            // 내용 동기화
            mobileBadge.textContent = originalBadge.textContent;

            // 표시 상태 동기화
            if (originalBadge.style.display === 'none') {
                mobileBadge.style.display = 'none';
            } else {
                mobileBadge.style.display = 'inline-flex';
            }
        }
    }

    // 페이지 로드 시 배지 상태 동기화
    syncUnreadChatCount();

    // WebSocket으로 새 메시지를 받았을 때 배지 상태 동기화 (만약 notification.js에서 updateUnreadCount 함수가 있다면)
    if (window.updateUnreadCount) {
        const originalUpdateUnreadCount = window.updateUnreadCount;
        window.updateUnreadCount = function(count) {
            originalUpdateUnreadCount(count);
            syncUnreadChatCount();
        };
    }
});
// 채팅 창 팝업 함수
function openChatWindow() {
    // 팝업 창의 크기와 위치 설정
    const width = 500;
    const height = 700;
    const left = (window.innerWidth - width) / 2;
    const top = (window.innerHeight - height) / 2;

    // 현재 사용자 ID가 필요한 경우 URL에 추가
    const userId = window.userId || '';

    // 팝업 창 열기
    const chatWindow = window.open(
        '/chat/view/rooms',
        'chatWindow',
        `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes,status=yes`
    );

    // 팝업 창 포커스
    if (chatWindow) {
        chatWindow.focus();
    } else {
        // 팝업이 차단된 경우 알림
        alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
    }
}