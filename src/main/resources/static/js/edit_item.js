document.addEventListener('DOMContentLoaded', function() {
    // 초기화 함수 호출
    setupImagePreview();
    setupTimeValidation();
    setupFormValidation();
});

// 이미지 미리보기 설정
function setupImagePreview() {
    const imageInput = document.getElementById('itemImage');
    if (!imageInput) return;

    const imagePreview = document.querySelector('.image-preview img');

    imageInput.addEventListener('change', function() {
        if (this.files && this.files[0]) {
            const reader = new FileReader();

            reader.onload = function(e) {
                imagePreview.src = e.target.result;
            };

            reader.readAsDataURL(this.files[0]);
        }
    });
}

// 시간 유효성 검사
function setupTimeValidation() {
    const startTimeSelect = document.getElementById('item_starttime');
    const endTimeSelect = document.getElementById('item_endtime');

    if (!startTimeSelect || !endTimeSelect) return;

    function validateTimeRange() {
        const startTime = parseInt(startTimeSelect.value);
        const endTime = parseInt(endTimeSelect.value);

        if (startTime >= endTime) {
            alert('종료 시간은 시작 시간보다 커야 합니다.');
            endTimeSelect.value = startTime + 1;
        }
    }

    startTimeSelect.addEventListener('change', function() {
        validateTimeRange();

        // 시작 시간이 변경되면 종료 시간 옵션 업데이트
        const startTime = parseInt(this.value);
        endTimeSelect.innerHTML = '';

        for (let i = startTime + 1; i <= 24; i++) {
            const option = document.createElement('option');
            option.value = i;
            option.textContent = i + ':00';
            endTimeSelect.appendChild(option);
        }

        endTimeSelect.value = Math.min(startTime + 1, 24);
    });

    endTimeSelect.addEventListener('change', validateTimeRange);
}

// 폼 제출 전 유효성 검사
function setupFormValidation() {
    const editForm = document.getElementById('editItemForm');
    if (!editForm) return;

    editForm.addEventListener('submit', function(event) {
        const startTimeSelect = document.getElementById('item_starttime');
        const endTimeSelect = document.getElementById('item_endtime');

        // 시간 유효성 검사
        if (startTimeSelect && endTimeSelect) {
            const startTime = parseInt(startTimeSelect.value);
            const endTime = parseInt(endTimeSelect.value);

            if (startTime >= endTime) {
                event.preventDefault();
                alert('종료 시간은 시작 시간보다 커야 합니다.');
                return false;
            }
        }

        return true;
    });
}