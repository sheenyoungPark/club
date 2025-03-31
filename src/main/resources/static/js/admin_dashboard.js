// 현재 시간 표시
    function updateTime() {
    const now = new Date();
    const formattedDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    document.getElementById('lastUpdate').textContent = formattedDate;
}

    // 시스템 상태 업데이트 함수
    function updateSystemStatus() {
    const systemStatusText = document.getElementById('systemStatusText');
    const systemStatusIcon = document.getElementById('systemStatusIcon');
    const systemStatusDetails = document.getElementById('systemStatusDetails');
    const systemStatusCard = systemStatusText.closest('.card.stat-card');

    // 현재 시간 가져오기
    const now = new Date();
    const timeString = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;

    // 시스템 상태 랜덤 시뮬레이션
    const random = Math.random();
    if (random > 0.97) { // 3% 확률로 주의 상태 표시
    systemStatusText.textContent = '주의';
    systemStatusIcon.className = 'fas fa-exclamation-triangle';
    systemStatusCard.classList.remove('bg-success', 'bg-danger');
    systemStatusCard.classList.add('bg-warning');
    systemStatusCard.classList.remove('text-white');
    systemStatusCard.classList.add('text-dark');
} else if (random > 0.99) { // 1% 확률로 경고 상태 표시
    systemStatusText.textContent = '경고';
    systemStatusIcon.className = 'fas fa-times-circle';
    systemStatusCard.classList.remove('bg-success', 'bg-warning');
    systemStatusCard.classList.add('bg-danger');
    systemStatusCard.classList.add('text-white');
    systemStatusCard.classList.remove('text-dark');
} else { // 96% 확률로 정상 상태 표시
    systemStatusText.textContent = '정상';
    systemStatusIcon.className = 'fas fa-server';
    systemStatusCard.classList.remove('bg-warning', 'bg-danger');
    systemStatusCard.classList.add('bg-success');
    systemStatusCard.classList.add('text-white');
    systemStatusCard.classList.remove('text-dark');
}

    // 서버 시간 업데이트
    systemStatusDetails.textContent = `서버 시간: ${timeString}`;
}

    // 회원 가입 추이 차트
    function initMembershipChart() {
    const ctx = document.getElementById('membershipChart').getContext('2d');

    // AJAX로 데이터 가져오기
    $.ajax({
    url: '/admin/monthly_member_counts',
    type: 'GET',
    success: function(response) {
    if (response.success) {
    const monthlyData = response.data;

    // 1월부터 12월까지의 라벨 생성
    const labels = Array.from({length: 12}, (_, i) => (i + 1) + '월');

    // 월별 회원 수 데이터 매핑
    const data = Array.from({length: 12}, (_, i) => {
    const monthItem = monthlyData.find(item => item.month === (i + 1));
    return monthItem ? monthItem.count : 0;
});

    const membershipChart = new Chart(ctx, {
    type: 'line',
    data: {
    labels: labels,
    datasets: [{
    label: '신규 회원',
    data: data,
    borderColor: 'rgb(13, 110, 253)',
    backgroundColor: 'rgba(13, 110, 253, 0.1)',
    tension: 0.3,
    fill: true
}]
},
    options: {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
    legend: {
    position: 'top',
},
    title: {
    display: false
}
},
    scales: {
    y: {
    beginAtZero: true
}
}
}
});
} else {
    console.error('데이터 로딩 오류:', response.message);
    initFallbackChart();
}
},
    error: function(xhr, status, error) {
    console.error('데이터 로딩 오류:', error);
    initFallbackChart();
}
});
}

    // 오류 발생 시 사용할 임시 차트
    function initFallbackChart() {
    const ctx = document.getElementById('membershipChart').getContext('2d');
    const membershipChart = new Chart(ctx, {
    type: 'line',
    data: {
    labels: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
    datasets: [{
    label: '신규 회원',
    data: [12, 19, 15, 17, 22, 28, 32, 35, 30, 25, 27, 33],
    borderColor: 'rgb(13, 110, 253)',
    backgroundColor: 'rgba(13, 110, 253, 0.1)',
    tension: 0.3,
    fill: true
}]
},
    options: {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
    legend: {
    position: 'top',
},
    title: {
    display: false
}
},
    scales: {
    y: {
    beginAtZero: true
}
}
}
});
}
    // 대기 중인 판매자 목록 불러오기 함수 수정
    function fetchPendingBusiness() {
    const pendingBusinessList = document.getElementById('pendingBusinessList');
    const loadingElement = document.getElementById('loadingBusinesses');

    // AJAX 요청으로 판매자 목록 가져오기 (API 경로 수정)
    $.ajax({
    url: '/admin/pending_business', // 새로 추가한 API 경로로 변경
    type: 'GET',
    success: function(response) {
    // 로딩 표시 제거
    if (loadingElement) {
    loadingElement.remove();
}

    // 응답이 성공하고 판매자 목록이 있는 경우
    if (response.success && response.businessList && response.businessList.length > 0) {
    response.businessList.forEach(function(business) {
    const listItem = document.createElement('li');
    listItem.className = 'list-group-item d-flex justify-content-between align-items-center py-2';

    // 판매자 정보
    const businessInfo = document.createElement('div');
    const businessName = document.createElement('div');
    businessName.className = 'fw-bold';
    businessName.textContent = business.business_name;

    const businessDetails = document.createElement('div');
    businessDetails.className = 'small text-muted';
    // 가입일 포맷팅 (ISO 문자열을 날짜 객체로 변환)
    const joinDate = new Date(business.business_joindate);
    const formattedDate = `${joinDate.getFullYear()}-${String(joinDate.getMonth() + 1).padStart(2, '0')}-${String(joinDate.getDate()).padStart(2, '0')}`;
    businessDetails.textContent = `ID: ${business.business_id} | 가입일: ${formattedDate}`;

    businessInfo.appendChild(businessName);
    businessInfo.appendChild(businessDetails);

    // 승인 버튼
    const actionDiv = document.createElement('div');
    const approveButton = document.createElement('button');
    approveButton.className = 'btn btn-sm btn-success me-1';
    approveButton.textContent = '승인';
    approveButton.dataset.businessId = business.business_id;
    approveButton.addEventListener('click', function() {
    approveBusiness(business.business_id, this);
});

    const detailButton = document.createElement('button');
    detailButton.className = 'btn btn-sm btn-secondary';
    detailButton.textContent = '상세';
    detailButton.dataset.businessId = business.business_id;
    detailButton.addEventListener('click', function() {
    viewBusinessDetail(business.business_id);
});

    actionDiv.appendChild(approveButton);
    actionDiv.appendChild(detailButton);

    // 리스트 아이템에 추가
    listItem.appendChild(businessInfo);
    listItem.appendChild(actionDiv);

    // 목록에 추가
    pendingBusinessList.appendChild(listItem);
});

    // 승인 대기 카운트 업데이트
    updatePendingCount();
} else {
    // 데이터가 없는 경우
    const emptyItem = document.createElement('li');
    emptyItem.className = 'list-group-item text-center py-3';
    emptyItem.textContent = '승인 대기 중인 판매자가 없습니다.';
    pendingBusinessList.appendChild(emptyItem);
}
},
    error: function(xhr, status, error) {
    // 오류 발생 시
    if (loadingElement) {
    loadingElement.remove();
}

    const errorItem = document.createElement('li');
    errorItem.className = 'list-group-item text-center text-danger py-3';
    errorItem.textContent = '판매자 목록을 불러오는데 실패했습니다.';
    pendingBusinessList.appendChild(errorItem);
    console.error('판매자 목록 로딩 오류:', error);
}
});
}

    // 판매자 승인 처리
    function approveBusiness(businessId, buttonElement) {
    if (confirm(`"${businessId}" 판매자를 승인하시겠습니까?`)) {
    $.ajax({
    url: '/admin/approve_business',
    type: 'POST',
    data: {
    businessId: businessId
},
    success: function(response) {
    if (response.success) {
    // 성공 시 해당 아이템 제거
    const listItem = buttonElement.closest('li');
    listItem.remove();

    // 승인 대기 카운트 업데이트 (카드에 표시된 숫자)
    updatePendingCount();

    alert(`"${businessId}" 판매자가 승인되었습니다.`);
} else {
    alert('승인 실패: ' + (response.message || '알 수 없는 오류가 발생했습니다.'));
}
},
    error: function(xhr, status, error) {
    alert('승인 처리 중 오류가 발생했습니다.');
    console.error('승인 오류:', error);
}
});
}
}
    // 승인 대기 카운트 로드 함수
    function loadPendingCounts() {
    // AJAX 요청으로 데이터 가져오기
    $.ajax({
        url: '/admin/pending_counts',
        type: 'GET',
        success: function(response) {
            if (response.success) {
                // 전체 승인 대기 건수 업데이트
                const pendingCountElement = document.getElementById('pendingCount');
                pendingCountElement.innerHTML = response.totalPending;

                // 오늘 신규 건수 업데이트
                const todayNewCountElement = document.getElementById('todayNewCount');
                todayNewCountElement.innerHTML = response.todayNew + '건';
            } else {
                console.error('승인 대기 건수 로드 실패:', response.message);

                // 에러 메시지 표시
                document.getElementById('pendingCount').innerHTML = '-';
                document.getElementById('todayNewCount').innerHTML = '-';
            }
        },
        error: function(xhr, status, error) {
            console.error('승인 대기 건수 로드 오류:', error);

            // 에러 메시지 표시
            document.getElementById('pendingCount').innerHTML = '-';
            document.getElementById('todayNewCount').innerHTML = '-';
        }
    });
}

    // 판매자 상세 정보 보기
    function viewBusinessDetail(businessId) {
    // 판매자 상세 정보 불러오기
    $.ajax({
        url: '/admin/business_detail',
        type: 'GET',
        data: {
            business_id: businessId
        },
        success: function(business) {
            if (business) {
                // 모달에 데이터 채우기 (모달 HTML을 추가해야 함)
                $('#businessDetailModal').modal('show');
                $('#modalBusinessId').text(business.business_id);
                $('#modalBusinessName').text(business.business_name);
                $('#modalBusinessEmail').text(business.business_email);
                $('#modalBusinessPhone').text(business.business_phone);
                $('#modalBusinessAddress').text(business.business_address);
                $('#modalBusinessNumber').text(business.business_number);

                // 가입일 포맷팅
                const joinDate = new Date(business.business_joindate);
                const formattedDate = `${joinDate.getFullYear()}-${String(joinDate.getMonth() + 1).padStart(2, '0')}-${String(joinDate.getDate()).padStart(2, '0')}`;
                $('#modalJoinDate').text(formattedDate);

                // 승인 버튼에 ID 설정
                $('#modalApproveBtn').data('businessId', business.business_id);
            } else {
                alert('판매자 정보를 불러올 수 없습니다.');
            }
        },
        error: function(xhr, status, error) {
            alert('판매자 상세 정보를 불러오는데 실패했습니다.');
            console.error('상세 정보 오류:', error);
        }
    });
}

    // 승인 대기 카운트 업데이트
    function updatePendingCount() {
    const pendingItems = document.querySelectorAll('#pendingBusinessList li:not(#loadingBusinesses)');
    const pendingCount = pendingItems.length;
    document.querySelector('.stat-card:nth-child(3) .fs-2').textContent = pendingCount;
}


    // 동호회 카테고리 분포 차트
    function initClubCategoryChart() {
    const ctx = document.getElementById('clubCategoryChart').getContext('2d');

    $.ajax({
    url: '/admin/club_category_distribution',
    type: 'GET',
    success: function(response) {
    if (response.success) {
    const categoryData = response.data;

    // 카테고리 라벨과 데이터 준비
    const labels = categoryData.map(item => item.category_type);
    const data = categoryData.map(item => item.club_count);

    // 데이터 개수에 맞게 색상 동적 생성
    const colors = generateColors(labels.length);

    const clubCategoryChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
    labels: labels,
    datasets: [{
    label: '동호회 수',
    data: data,
    backgroundColor: colors,
    borderWidth: 1
}]
},
    options: {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
    legend: {
    position: 'right',
    labels: {
    boxWidth: 10,
    font: {
    size: 10
}
}
}
}
}
});
} else {
    console.error('데이터 로딩 오류:', response.message);
    initFallbackCategoryChart();
}
},
    error: function(xhr, status, error) {
    console.error('데이터 로딩 오류:', error);
    initFallbackCategoryChart();
}
});
}

    // 색상을 동적으로 생성하는 함수
    function generateColors(count) {
    const colors = [];
    for (let i = 0; i < count; i++) {
    // HSL 색상 모델을 사용해 고르게 분포된 색상 생성
    const hue = (i * 360 / count) % 360;
    colors.push(`hsla(${hue}, 70%, 60%, 0.8)`);
}
    return colors;
}

    // 대기 중인 클럽 목록 불러오기 함수
    function fetchPendingClubs() {
    const pendingClubList = document.getElementById('pendingClubList');
    const loadingElement = document.getElementById('loadingClubs');

    // AJAX 요청으로 대기 중인 클럽 목록 가져오기
    $.ajax({
    url: '/admin/pending_clubs', // 이 API 엔드포인트는 새로 만들어야 합니다
    type: 'GET',
    success: function(response) {
    // 로딩 표시 제거
    if (loadingElement) {
    loadingElement.remove();
}

    // 응답이 성공하고 클럽 목록이 있는 경우
    if (response.success && response.clubList && response.clubList.length > 0) {
    response.clubList.forEach(function(club) {
    const listItem = document.createElement('li');
    listItem.className = 'list-group-item d-flex justify-content-between align-items-center py-2';

    // 클럽 정보
    const clubInfo = document.createElement('div');
    const clubName = document.createElement('div');
    clubName.className = 'fw-bold';
    clubName.textContent = club.club_name;

    const clubDetails = document.createElement('div');
    clubDetails.className = 'small text-muted';
    // 가입일 포맷팅
    const joinDate = new Date(club.club_joindate);
    const formattedDate = `${joinDate.getFullYear()}-${String(joinDate.getMonth() + 1).padStart(2, '0')}-${String(joinDate.getDate()).padStart(2, '0')}`;
    clubDetails.textContent = `카테고리: ${club.club_category} | 가입일: ${formattedDate}`;

    clubInfo.appendChild(clubName);
    clubInfo.appendChild(clubDetails);

    // 승인 버튼
    const actionDiv = document.createElement('div');
    const approveButton = document.createElement('button');
    approveButton.className = 'btn btn-sm btn-success me-1';
    approveButton.textContent = '승인';
    approveButton.dataset.clubId = club.club_id;
    approveButton.addEventListener('click', function() {
    approveClub(club.club_id, this);
});

    const detailButton = document.createElement('button');
    detailButton.className = 'btn btn-sm btn-secondary';
    detailButton.textContent = '상세';
    detailButton.dataset.clubId = club.club_id;
    detailButton.addEventListener('click', function() {
    viewClubDetail(club.club_id);
});

    actionDiv.appendChild(approveButton);
    actionDiv.appendChild(detailButton);

    // 리스트 아이템에 추가
    listItem.appendChild(clubInfo);
    listItem.appendChild(actionDiv);

    // 목록에 추가
    pendingClubList.appendChild(listItem);
});

    // 승인 대기 카운트 업데이트
    updatePendingClubCount();
} else {
    // 데이터가 없는 경우
    const emptyItem = document.createElement('li');
    emptyItem.className = 'list-group-item text-center py-3';
    emptyItem.textContent = '승인 대기 중인 클럽이 없습니다.';
    pendingClubList.appendChild(emptyItem);
}
},
    error: function(xhr, status, error) {
    // 오류 발생 시
    if (loadingElement) {
    loadingElement.remove();
}

    const errorItem = document.createElement('li');
    errorItem.className = 'list-group-item text-center text-danger py-3';
    errorItem.textContent = '클럽 목록을 불러오는데 실패했습니다.';
    pendingClubList.appendChild(errorItem);
    console.error('클럽 목록 로딩 오류:', error);
}
});
}

    // 클럽 승인 처리
    function approveClub(clubId, buttonElement) {
    if (confirm(`클럽 ID ${clubId}를 승인하시겠습니까?`)) {
    $.ajax({
    url: '/admin/approve_club',
    type: 'POST',
    data: {
    clubId: clubId
},
    success: function(response) {
    if (response.success) {
    // 성공 시 해당 아이템 제거
    const listItem = buttonElement.closest('li');
    listItem.remove();

    // 승인 대기 카운트 업데이트
    updatePendingClubCount();

    alert(`클럽 ID ${clubId}가 승인되었습니다.`);
} else {
    alert('승인 실패: ' + (response.message || '알 수 없는 오류가 발생했습니다.'));
}
},
    error: function(xhr, status, error) {
    alert('승인 처리 중 오류가 발생했습니다.');
    console.error('승인 오류:', error);
}
});
}
}

    // 클럽 상세 정보 보기
    function viewClubDetail(clubId) {
    // 클럽 상세 정보 불러오기
    $.ajax({
        url: '/admin/club_detail',
        type: 'GET',
        data: {
            club_id: clubId
        },
        success: function(club) {
            if (club) {
                // 모달에 데이터 채우기 (모달 HTML을 추가해야 함)
                $('#clubDetailModal').modal('show');
                $('#modalClubId').text(club.club_id);
                $('#modalClubName').text(club.club_name);
                $('#modalClubCategory').text(club.club_category);
                $('#modalClubInfo').text(club.club_info);
                $('#modalClubRegion').text(club.club_region || '정보 없음');
                $('#modalClubAgeMin').text(club.club_agemin || '제한 없음');

                // 가입일 포맷팅
                const joinDate = new Date(club.club_joindate);
                const formattedDate = `${joinDate.getFullYear()}-${String(joinDate.getMonth() + 1).padStart(2, '0')}-${String(joinDate.getDate()).padStart(2, '0')}`;
                $('#modalJoinDate').text(formattedDate);

                // 승인 버튼에 ID 설정
                $('#modalApproveBtn').data('clubId', club.club_id);

                // 클럽 프로필 이미지가 있는 경우 표시
                if (club.club_profile) {
                    $('#modalClubProfile').attr('src', '/upload/clubProfile/' + club.club_profile);
                    $('#modalClubProfileContainer').show();
                } else {
                    $('#modalClubProfileContainer').hide();
                }
            } else {
                alert('클럽 정보를 불러올 수 없습니다.');
            }
        },
        error: function(xhr, status, error) {
            alert('클럽 상세 정보를 불러오는데 실패했습니다.');
            console.error('상세 정보 오류:', error);
        }
    });
}

    // 승인 대기 카운트 업데이트
    function updatePendingClubCount() {
    const pendingItems = document.querySelectorAll('#pendingClubList li:not(#loadingClubs)');
    const pendingCount = pendingItems.length;
    // 대시보드의 승인 대기 카드 업데이트
    document.querySelector('.stat-card:nth-child(3) .fs-2').textContent = pendingCount;
}

    // DOM이 로드되면 실행
    document.addEventListener('DOMContentLoaded', function() {
    // 현재 시간 표시 및 업데이트
    updateTime();
    setInterval(updateTime, 1000);

    // 차트 초기화
    initMembershipChart();
    initClubCategoryChart();

    fetchPendingBusiness();
    fetchPendingClubs();
    loadPendingCounts();

    // 시스템 상태 초기 업데이트
    updateSystemStatus();

    // 1초마다 시스템 상태 업데이트
    setInterval(updateSystemStatus, 1000);

    // // 10분마다 자동 업데이트
    // setInterval(loadPendingCounts, 300000);

    // 모달 승인 버튼 이벤트 추가
    if (document.getElementById('modalApproveBtn')) {
    document.getElementById('modalApproveBtn').addEventListener('click', function() {
    const clubId = this.dataset.clubId;
    if (clubId) {
    // 승인 처리
    approveClub(clubId, this);
    // 모달 닫기
    const modal = bootstrap.Modal.getInstance(document.getElementById('clubDetailModal'));
    modal.hide();
}
});
}
});
    // 모달 승인 버튼 이벤트 추가
    document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('modalApproveBtn').addEventListener('click', function() {
        const businessId = this.dataset.businessId;
        if (businessId) {
            // 승인 처리
            approveBusiness(businessId, this);
            // 모달 닫기
            const modal = bootstrap.Modal.getInstance(document.getElementById('businessDetailModal'));
            modal.hide();
        }
    });
});
// 채팅 창 팝업 함수
function openChatWindow() {
    // 팝업 창의 크기와 위치 설정
    const width = 800;
    const height = 600;
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
