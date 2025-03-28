// 예약 유형 관련 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // 기존 변수들
    const personalRadio = document.getElementById('personal_reservation');
    const clubRadio = document.getElementById('club_reservation');
    const clubSelect = document.getElementById('club_id');
    const clubPointInfo = document.querySelector('.club-point-info');
    const clubSelection = document.querySelector('.club-selection');

    // 새로운 변수들 (카드 선택용)
    const personalCard = document.getElementById('personal-card');
    const clubCard = document.getElementById('club-card');

    // 초기 상태 설정
    personalCard.classList.add('selected');

    // 카드 클릭 이벤트 리스너
    personalCard.addEventListener('click', function() {
        personalRadio.checked = true;
        updateCardSelection();
        handleReservationTypeChange();
    });

    clubCard.addEventListener('click', function() {
        clubRadio.checked = true;
        updateCardSelection();
        handleReservationTypeChange();
    });

    // 라디오 버튼 변경 이벤트 리스너
    personalRadio.addEventListener('change', function() {
        updateCardSelection();
        handleReservationTypeChange();
    });

    clubRadio.addEventListener('change', function() {
        updateCardSelection();
        handleReservationTypeChange();
    });

    // 카드 선택 상태 업데이트 함수
    function updateCardSelection() {
        if (personalRadio.checked) {
            personalCard.classList.add('selected');
            clubCard.classList.remove('selected');
        } else {
            clubCard.classList.add('selected');
            personalCard.classList.remove('selected');
        }
    }

    // 예약 유형 변경 처리 함수 - 기존 함수를 확장
    function handleReservationTypeChange() {
        if (clubRadio.checked) {
            clubSelection.style.display = 'block';
            clubSelect.disabled = false;
            clubSelect.required = true;

            // 슬라이드 다운 애니메이션 효과 (CSS 트랜지션과 함께 작동)
            clubSelection.style.maxHeight = clubSelection.scrollHeight + "px";
            clubSelection.style.opacity = 1;

            // 클럽 선택이 유효하면 클럽 포인트 정보 표시
            if (clubSelect.value) {
                clubPointInfo.style.display = 'block';
            }

            // AJAX로 사용자가 마스터인 클럽 목록 가져오기
            fetchMasterClubs();
        } else {
            // 슬라이드 업 애니메이션 효과
            clubSelection.style.maxHeight = 0;
            clubSelection.style.opacity = 0;

            // 트랜지션 후 실제로 숨김 처리
            setTimeout(() => {
                clubSelection.style.display = 'none';
                clubSelect.disabled = true;
                clubSelect.required = false;
                clubPointInfo.style.display = 'none';
            }, 300); // CSS 트랜지션과 동일한 시간으로 설정
        }

        // 가격 및 유효성 다시 계산
        calculatePrice();
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const dateInput = document.getElementById('reservation_date');
    const startTimeSelect = document.getElementById('start_time');
    const endTimeSelect = document.getElementById('end_time');
    const reservationHoursElement = document.getElementById('reservationHours');
    const totalPriceElement = document.getElementById('totalPrice');
    const reserveButton = document.getElementById('reserveButton');
    const itemId = document.getElementById('item_id').value;
    const hourlyPrice = parseInt(document.getElementById('hourlyPrice').value);
    const reservationForm = document.getElementById('reservationForm');
    const reservedTimesList = document.getElementById('reservedTimesList');

    // 클럽 예약 관련 요소들
    const personalRadio = document.getElementById('personal_reservation');
    const clubRadio = document.getElementById('club_reservation');
    const clubSelect = document.getElementById('club_id');
    const clubPointInfo = document.querySelector('.club-point-info');
    const clubSelection = document.querySelector('.club-selection');

    // 이미 예약된 시간대를 저장할 배열
    let reservedTimeRanges = [];

    // 예약 유형 변경 이벤트 설정
    personalRadio.addEventListener('change', handleReservationTypeChange);
    clubRadio.addEventListener('change', handleReservationTypeChange);

    // 예약 유형 변경 처리 함수
    function handleReservationTypeChange() {
        if (clubRadio.checked) {
            clubSelection.style.display = 'block';
            clubSelect.disabled = false;
            clubSelect.required = true;

            // 클럽 선택이 유효하면 클럽 포인트 정보 표시
            if (clubSelect.value) {
                clubPointInfo.style.display = 'block';
            }

            // AJAX로 사용자가 마스터인 클럽 목록 가져오기
            fetchMasterClubs();
        } else {
            clubSelection.style.display = 'none';
            clubSelect.disabled = true;
            clubSelect.required = false;
            clubPointInfo.style.display = 'none';
        }

        // 가격 및 유효성 다시 계산
        calculatePrice();
    }

    // 사용자가 마스터인 클럽 목록 가져오기
    function fetchMasterClubs() {
        // 로딩 메시지 표시
        clubSelect.innerHTML = '<option value="">클럽 목록을 불러오는 중...</option>';

        fetch('/reservation/api/clubs/master')
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(clubs => {
                console.log('받은 클럽 목록:', clubs); // 디버깅용

                // 기본 옵션 추가
                clubSelect.innerHTML = '<option value="">클럽을 선택하세요</option>';

                // 클럽 목록 추가
                if (Array.isArray(clubs) && clubs.length > 0) {
                    clubs.forEach(club => {
                        const option = document.createElement('option');
                        option.value = club.club_id || club.CLUB_ID;
                        option.textContent = club.club_name || club.CLUB_NAME;
                        option.dataset.points = club.club_point || club.CLUB_POINT;
                        clubSelect.appendChild(option);
                    });
                } else {
                    const option = document.createElement('option');
                    option.value = "";
                    option.textContent = "마스터 권한이 있는 클럽이 없습니다";
                    option.disabled = true;
                    clubSelect.appendChild(option);
                }
            })
            .catch(error => {
                console.error('클럽 목록을 가져오는 중 오류 발생:', error);
                clubSelect.innerHTML = '<option value="">클럽 목록을 불러오는데 실패했습니다</option>';
            });
    }

    // 클럽 선택 변경 이벤트 처리
    clubSelect.addEventListener('change', function() {
        const selectedOption = this.options[this.selectedIndex];

        if (selectedOption && selectedOption.dataset.points) {
            // 선택한 클럽의 포인트 표시
            document.getElementById('clubPointDisplay').textContent =
                Number(selectedOption.dataset.points).toLocaleString() + '포인트';
            clubPointInfo.style.display = 'block';
        } else {
            clubPointInfo.style.display = 'none';
        }

        // 가격 다시 계산
        calculatePrice();
    });

    // 날짜 변경 시 예약 가능 시간 업데이트
    dateInput.addEventListener('change', function() {
        updateAvailableTimes();
    });

    // 시작 시간 변경 시 종료 시간 옵션 업데이트
    startTimeSelect.addEventListener('change', function() {
        updateEndTimeOptions();
        calculatePrice();
    });

    // 종료 시간 변경 시 가격 계산
    endTimeSelect.addEventListener('change', function() {
        calculatePrice();
    });

    // 폼 제출 전 유효성 검증
    reservationForm.addEventListener('submit', function(event) {
        if (clubRadio.checked && clubSelect.value) {
            const selectedOption = clubSelect.options[clubSelect.selectedIndex];
            const clubPoints = selectedOption.dataset.points ? parseInt(selectedOption.dataset.points) : 0;
            const totalPrice = parseInt(totalPriceElement.textContent.replace(/[^0-9]/g, ''));

            // 포인트가 부족한 경우 제출 방지
            if (totalPrice > clubPoints) {
                event.preventDefault();
                alert('클럽 포인트가 부족합니다.');
            }
        }
    });

    // 페이지 로드 시 현재 날짜를 yyyy-MM-dd 형식으로 설정하고 예약 정보 불러오기
    function setInitialDate() {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');

        dateInput.value = `${year}-${month}-${day}`;

        // 초기 날짜에 대한 예약 정보 불러오기
        updateAvailableTimes();
    }

    // 예약 가능 시간 업데이트 함수
    function updateAvailableTimes() {
        // 날짜 입력값을 가져와 정확한 형식으로 변환
        const rawDate = dateInput.value;
        const dateObj = new Date(rawDate);
        const year = dateObj.getFullYear();
        const month = String(dateObj.getMonth() + 1).padStart(2, '0');
        const day = String(dateObj.getDate()).padStart(2, '0');
        const formattedDate = `${year}-${month}-${day}`;

        console.log("서버에 보내는 날짜:", formattedDate); // 디버깅용

        // 예약된 시간 표시 영역 초기화
        reservedTimesList.innerHTML = '<p class="loading-message">예약된 시간을 불러오는 중...</p>';

        // 모든 시간 옵션 초기화
        resetTimeOptions(startTimeSelect);
        resetTimeOptions(endTimeSelect);

        // 예약 버튼 비활성화
        reserveButton.disabled = true;

        // 로딩 중 표시
        startTimeSelect.disabled = true;
        endTimeSelect.disabled = true;

        // API 호출
        fetch(`/reservation/check-availability?item_id=${itemId}&date=${encodeURIComponent(formattedDate)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                // 응답 데이터 로깅 (디버깅용)
                console.log('API 응답:', data);

                startTimeSelect.disabled = false;

                if (data.success) {
                    // 데이터 구조 확인 및 저장
                    reservedTimeRanges = Array.isArray(data.reservedTimeRanges) ? data.reservedTimeRanges : [];

                    // 예약된 시간이 없으면 빈 배열로 처리
                    if (reservedTimeRanges.length === 0) {
                        console.log('예약된 시간이 없습니다.');
                    } else {
                        console.log('예약된 시간 범위:', reservedTimeRanges);
                    }

                    // 예약된 시간 목록 표시
                    displayReservedTimes(reservedTimeRanges, formattedDate);

                    // 예약된 시간 범위에 따라 시간 옵션 설정
                    updateTimeOptions();
                } else {
                    console.error('예약 가능 시간 확인 실패:', data.error);
                    alert('예약 가능 시간을 불러오는데 실패했습니다: ' + (data.error || '알 수 없는 오류'));
                    reservedTimesList.innerHTML = '<p class="error-message">예약 정보를 불러오는데 실패했습니다.</p>';
                }
            })
            .catch(error => {
                console.error('예약 가능 시간 확인 중 오류 발생:', error);
                alert('서버와 통신 중 오류가 발생했습니다: ' + error.message);
                reservedTimesList.innerHTML = '<p class="error-message">서버와 통신 중 오류가 발생했습니다.</p>';
                startTimeSelect.disabled = false;
            });
    }

    // 예약된 시간 목록 표시 함수
    function displayReservedTimes(reservedTimeRanges, date) {
        // 날짜 형식화 (YYYY-MM-DD → YYYY년 MM월 DD일)
        const [year, month, day] = date.split('-');
        const formattedDate = `${year}년 ${parseInt(month)}월 ${parseInt(day)}일`;

        if (!Array.isArray(reservedTimeRanges) || reservedTimeRanges.length === 0) {
            reservedTimesList.innerHTML = `<p class="no-reservations">${formattedDate}에는 예약된 시간이 없습니다.</p>`;
            return;
        }

        // API 응답 로깅
        console.log("원본 API 응답 데이터:", JSON.stringify(reservedTimeRanges));

        // 시간별로 정렬
        const sortedRanges = [...reservedTimeRanges].sort((a, b) => {
            // 오라클 DB에서 반환되는 컬럼명을 그대로 확인
            const startTimeA = a.START_TIME || a.start_time || a.startTime || 0;
            const startTimeB = b.START_TIME || b.start_time || b.startTime || 0;
            return startTimeA - startTimeB;
        });

        // HTML 생성
        let html = `<h5>${formattedDate} 예약 현황</h5>`;
        html += '<ul class="reserved-times">';

        // 디버깅용 로그
        console.log("표시할 예약 범위 (정렬 후):", sortedRanges);

        sortedRanges.forEach(range => {
            // 오라클 DB에서 반환되는 컬럼명을 고려한 처리 (대문자일 수 있음)
            const startTime = range.START_TIME || range.start_time || range.startTime;
            const endTime = range.END_TIME || range.end_time || range.endTime;

            console.log(`범위 처리 중: startTime=${startTime}, endTime=${endTime}`);

            if (startTime !== undefined && endTime !== undefined) {
                html += `<li class="reserved-time-item">
                    <span class="reserved-time-range">${startTime}:00 ~ ${endTime}:00</span>
                    <span class="reserved-time-status">예약됨</span>
                </li>`;
            } else {
                console.warn("유효하지 않은 시간 범위 데이터:", range);
            }
        });

        html += '</ul>';
        reservedTimesList.innerHTML = html;
    }

    // 시간 옵션 초기화 함수
    function resetTimeOptions(selectElement) {
        // 첫 번째 옵션(라벨)을 제외한 모든 옵션 활성화
        for (let i = 1; i < selectElement.options.length; i++) {
            selectElement.options[i].disabled = false;
        }

        // 기본 옵션(첫 번째) 선택
        selectElement.selectedIndex = 0;
    }

    // 현재 시간 기준으로 지난 시간 비활성화 함수
    function disablePastTimes() {
        const selectedDate = new Date(dateInput.value);
        const now = new Date();

        // 선택된 날짜가 오늘인지 확인
        const isToday = selectedDate.getDate() === now.getDate() &&
            selectedDate.getMonth() === now.getMonth() &&
            selectedDate.getFullYear() === now.getFullYear();

        if (isToday) {
            const currentHour = now.getHours();

            // 시작 시간 옵션에서 현재 시간 이전의 옵션 비활성화
            for (let i = 1; i < startTimeSelect.options.length; i++) {
                const optionValue = parseInt(startTimeSelect.options[i].value);
                if (optionValue <= currentHour) {
                    startTimeSelect.options[i].disabled = true;
                    console.log(`${optionValue}:00 시간은 이미 지났습니다. 비활성화합니다.`);
                }
            }

            // 종료 시간 옵션도 함께 업데이트
            if (startTimeSelect.value) {
                updateEndTimeOptions();
            }
        }
    }

    // 예약된 시간에 따라 옵션 업데이트
    function updateTimeOptions() {
        console.log('시간 옵션 업데이트 시작');

        // 모든 시작 시간 옵션 초기화 (활성화)
        for (let i = 1; i < startTimeSelect.options.length; i++) {
            startTimeSelect.options[i].disabled = false;
        }

        // 예약된 시간 범위에 따라 시작 시간 옵션 비활성화
        if (Array.isArray(reservedTimeRanges) && reservedTimeRanges.length > 0) {
            reservedTimeRanges.forEach(range => {
                console.log('처리 중인 예약 범위:', range);

                // 다양한 속성 이름 처리
                const startTime = typeof range.start_time !== 'undefined' ? range.start_time :
                    (typeof range.startTime !== 'undefined' ? range.startTime :
                        (typeof range.START_TIME !== 'undefined' ? range.START_TIME : null));

                const endTime = typeof range.end_time !== 'undefined' ? range.end_time :
                    (typeof range.endTime !== 'undefined' ? range.endTime :
                        (typeof range.END_TIME !== 'undefined' ? range.END_TIME : null));

                if (startTime !== null && endTime !== null) {
                    console.log(`범위 ${startTime}:00 ~ ${endTime}:00 처리 중`);

                    // 시작 시간 옵션 중에서 예약된 범위에 포함되는 시간 비활성화
                    for (let i = 1; i < startTimeSelect.options.length; i++) {
                        const optionValue = parseInt(startTimeSelect.options[i].value);
                        if (optionValue >= startTime && optionValue < endTime) {
                            console.log(`${optionValue}:00 시작 시간 비활성화`);
                            startTimeSelect.options[i].disabled = true;
                        }
                    }
                } else {
                    console.warn('시간 범위에 유효한 시작/종료 시간이 없습니다:', range);
                }
            });
        }

        // 현재 시간 이전의 시간 비활성화
        disablePastTimes();

        // 종료 시간 업데이트 (시작 시간이 이미 선택되어 있는 경우)
        if (startTimeSelect.value) {
            updateEndTimeOptions();
        }

        console.log('시간 옵션 업데이트 완료');
    }

    // 종료 시간 옵션 업데이트 함수
    function updateEndTimeOptions() {
        // 시작 시간이 선택되지 않았으면 종료 시간 옵션 비활성화
        if (!startTimeSelect.value) {
            endTimeSelect.disabled = true;
            resetTimeOptions(endTimeSelect);
            return;
        }

        // 종료 시간 선택 활성화
        endTimeSelect.disabled = false;
        // 종료 시간 옵션 초기화
        resetTimeOptions(endTimeSelect);

        const startTime = parseInt(startTimeSelect.value);

        // 다음 예약까지 가능한 최대 종료 시간 찾기
        let maxEndTime = null;

        if (Array.isArray(reservedTimeRanges) && reservedTimeRanges.length > 0) {
            reservedTimeRanges.forEach(range => {
                // 오라클 DB에서 반환되는 컬럼명을 고려한 처리 (대문자일 수 있음)
                const rangeStartTime = range.START_TIME || range.start_time || range.startTime;

                if (rangeStartTime !== undefined && rangeStartTime > startTime && (maxEndTime === null || rangeStartTime < maxEndTime)) {
                    maxEndTime = rangeStartTime;
                }
            });
        }

        // 종료 시간 옵션 설정
        for (let i = 1; i < endTimeSelect.options.length; i++) {
            const optionValue = parseInt(endTimeSelect.options[i].value);

            // 시작 시간보다 작거나 같으면 비활성화
            if (optionValue <= startTime) {
                endTimeSelect.options[i].disabled = true;
            }

            // 다음 예약 시간 이후면 비활성화
            if (maxEndTime !== null && optionValue > maxEndTime) {
                endTimeSelect.options[i].disabled = true;
            }
        }

        // 기본값으로 시작 시간 + 1 선택
        let defaultOptionFound = false;
        for (let i = 1; i < endTimeSelect.options.length; i++) {
            const optionValue = parseInt(endTimeSelect.options[i].value);
            if (optionValue === startTime + 1 && !endTimeSelect.options[i].disabled) {
                endTimeSelect.options[i].selected = true;
                defaultOptionFound = true;
                break;
            }
        }

        // 기본 옵션을 찾지 못했다면 첫 번째 활성화된 옵션 선택
        if (!defaultOptionFound) {
            for (let i = 1; i < endTimeSelect.options.length; i++) {
                if (!endTimeSelect.options[i].disabled) {
                    endTimeSelect.options[i].selected = true;
                    break;
                }
            }
        }

        // 가격 계산
        calculatePrice();
    }

    // 가격 계산 함수 - 클럽 예약도 처리하도록 확장
    function calculatePrice() {
        if (!startTimeSelect.value || !endTimeSelect.value) {
            reservationHoursElement.textContent = "0시간";
            totalPriceElement.textContent = "0원";
            reserveButton.disabled = true;
            return;
        }

        const startTime = parseInt(startTimeSelect.value);
        const endTime = parseInt(endTimeSelect.value);
        const hours = endTime - startTime;

        if (hours <= 0) {
            reservationHoursElement.textContent = "0시간";
            totalPriceElement.textContent = "0원";
            reserveButton.disabled = true;
            return;
        }

        const totalPrice = hours * hourlyPrice;

        reservationHoursElement.textContent = hours + "시간";
        totalPriceElement.textContent = totalPrice.toLocaleString() + "원";

        // 클럽 예약 선택 시 포인트 확인
        if (clubRadio && clubRadio.checked && clubSelect && clubSelect.value) {
            const selectedOption = clubSelect.options[clubSelect.selectedIndex];
            const clubPoints = selectedOption.dataset.points ? parseInt(selectedOption.dataset.points) : 0;

            // 포인트가 부족한 경우 예약 버튼 비활성화
            if (totalPrice > clubPoints) {
                reserveButton.disabled = true;
                reserveButton.title = '클럽 포인트가 부족합니다';
                // 클럽 포인트 정보에 스타일 추가
                if (clubPointInfo) {
                    clubPointInfo.classList.add('insufficient-points');
                }
                return;
            } else {
                // 충분한 포인트가 있으면 스타일 제거
                if (clubPointInfo) {
                    clubPointInfo.classList.remove('insufficient-points');
                }
            }
        }

        // 유효한 시간 범위와 가격이 있으면 예약 버튼 활성화
        reserveButton.disabled = false;
        reserveButton.title = '';
    }

    // 초기화 함수 호출
    setInitialDate();
});
// 문의하기 팝업 함수 및 이벤트 리스너
document.addEventListener('DOMContentLoaded', function() {
    // 문의하기 버튼 이벤트 리스너 추가
    const inquiryButtons = document.querySelectorAll('.inquiry-btn');

    if (inquiryButtons.length > 0) {
        inquiryButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();

                // 데이터 속성에서 ID 값 가져오기
                const itemId = this.getAttribute('data-item-id');
                const businessId = this.getAttribute('data-business-id');

                // 팝업 창 열기
                openInquiryWindow(itemId, businessId);
            });
        });
    }
});

// 문의하기 팝업 창 함수
function openInquiryWindow(itemId, businessId) {
    // 팝업 창의 크기와 위치 설정
    const width = 700;
    const height = 600;
    const left = (window.innerWidth - width) / 2;
    const top = (window.innerHeight - height) / 2;

    // URL 생성
    const url = `/chat/new/ask?item_id=${itemId}&business_id=${businessId}`;

    // 팝업 창 열기
    const inquiryWindow = window.open(
        url,
        'inquiryWindow',
        `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes,status=yes`
    );

    // 팝업 창 포커스
    if (inquiryWindow) {
        inquiryWindow.focus();
    } else {
        // 팝업이 차단된 경우 알림
        alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
    }
}