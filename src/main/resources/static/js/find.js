// find.js
document.addEventListener('DOMContentLoaded', function() {
    // 비밀번호 찾기 페이지에서 비밀번호 확인 로직
    const resetPasswordForm = document.getElementById('reset-password-form');
    if (resetPasswordForm) {
        resetPasswordForm.addEventListener('submit', function(e) {
            const newPassword = document.getElementById('new_password').value;
            const confirmPassword = document.getElementById('confirm_password').value;

            // 비밀번호 일치 여부 확인
            if (newPassword !== confirmPassword) {
                e.preventDefault();
                alert('비밀번호가 일치하지 않습니다. 다시 확인해주세요.');
                return false;
            }

            // 비밀번호 규칙 검증 (8자 이상)
            if (newPassword.length < 8) {
                e.preventDefault();
                alert('비밀번호는 8자 이상이어야 합니다.');
                return false;
            }

            return true;
        });
    }

    // 휴대폰 번호 입력 시 형식 처리
    // 사용자가 입력할 때는 숫자만 입력하도록 하고, 서버로 전송할 때도 숫자만 전송
    // DB에서는 010-1111-2222 형식으로 저장되어 있으므로 서버에서 변환 처리
    const phoneInputs = document.querySelectorAll('input[name="phone"]');
    if (phoneInputs.length > 0) {
        phoneInputs.forEach(input => {
            input.addEventListener('input', function(e) {
                // 숫자만 남기고 모든 특수문자 제거
                this.value = this.value.replace(/[^0-9]/g, '');
            });

            // 입력 시 실시간으로 하이픈을 표시하지만 전송 시에는 숫자만 전송하려면 아래 주석을 해제
            /*
            input.addEventListener('keyup', function(e) {
                const value = this.value.replace(/[^0-9]/g, '');

                if (value.length <= 3) {
                    this.value = value;
                } else if (value.length <= 7) {
                    this.value = value.substring(0, 3) + '-' + value.substring(3);
                } else {
                    this.value = value.substring(0, 3) + '-' + value.substring(3, 7) + '-' + value.substring(7, 11);
                }
            });
            */
        });
    }

    // 결과 메시지가 있는 경우 5초 후 자동으로 사라지게 하기
    const alertMessages = document.querySelectorAll('.alert');
    if (alertMessages.length > 0) {
        setTimeout(function() {
            alertMessages.forEach(alert => {
                alert.style.transition = 'opacity 1s';
                alert.style.opacity = '0';

                setTimeout(function() {
                    alert.style.display = 'none';
                }, 1000);
            });
        }, 5000);
    }
});