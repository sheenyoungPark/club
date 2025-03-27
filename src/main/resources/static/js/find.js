document.addEventListener('DOMContentLoaded', function() {
    // 비밀번호 재설정 로직 (개인 및 기업회원 공통)
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

            // 비밀번호 길이 검증 (8자 이상)
            if (newPassword.length < 8) {
                e.preventDefault();
                alert('비밀번호는 8자 이상이어야 합니다.');
                return false;
            }

            // 현재 페이지가 기업회원인지 개인회원인지 확인
            const isBusinessPage = window.location.pathname.includes('/business/');

            // 기업회원 페이지인 경우 16자 이하 제한 적용
            if (isBusinessPage && newPassword.length > 16) {
                e.preventDefault();
                alert('비밀번호는 16자 이하여야 합니다.');
                return false;
            }

            // 개인회원 페이지인 경우 영문, 숫자, 특수문자 포함 검증
            if (!isBusinessPage) {
                const hasLetter = /[a-zA-Z]/.test(newPassword);
                const hasNumber = /[0-9]/.test(newPassword);
                const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(newPassword);

                if (!(hasLetter && hasNumber && hasSpecial)) {
                    e.preventDefault();
                    alert('비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.');
                    return false;
                }
            }

            return true;
        });
    }

    // 본인확인 폼 숨기기 로직
    const findPasswordForm = document.getElementById('find-password-form');
    const resetPasswordFormExists = document.getElementById('reset-password-form');

    // 재설정 폼이 표시되고 있으면 본인확인 폼 숨기기
    if (findPasswordForm && resetPasswordFormExists && resetPasswordFormExists.style.display !== 'none') {
        findPasswordForm.style.display = 'none';
    }

    // 휴대폰 번호 입력 시 형식 처리 (개인 및 기업 공통)
    const phoneInputs = document.querySelectorAll('input[name="phone"], input[name="business_phone"]');
    if (phoneInputs.length > 0) {
        phoneInputs.forEach(input => {
            input.addEventListener('input', function(e) {
                // 숫자만 남기고 모든 특수문자 제거
                this.value = this.value.replace(/[^0-9]/g, '');
            });
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