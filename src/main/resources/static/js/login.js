document.addEventListener('DOMContentLoaded', function() {
    // Tab switching functionality
    const tabs = document.querySelectorAll('.tab');
    const personalForm = document.getElementById('personal-form');
    const businessForm = document.getElementById('business-form');

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // Remove active class from all tabs
            tabs.forEach(t => t.classList.remove('active'));

            // Add active class to clicked tab
            this.classList.add('active');

            // Show/hide appropriate form
            const tabType = this.getAttribute('data-tab');
            if (tabType === 'personal') {
                personalForm.style.display = 'block';
                businessForm.style.display = 'none';
            } else if (tabType === 'business') {
                personalForm.style.display = 'none';
                businessForm.style.display = 'block';
            }
        });
    });

    // Form validation
    const validateForm = (form) => {
        let isValid = true;
        const inputs = form.querySelectorAll('input');

        inputs.forEach(input => {
            if (input.value.trim() === '') {
                input.style.borderColor = '#dc3545';
                isValid = false;
            } else {
                input.style.borderColor = '#ddd';
            }
        });

        return isValid;
    };

    // Add validation to personal form
    if (personalForm) {
        personalForm.addEventListener('submit', function(e) {
            if (!validateForm(this)) {
                e.preventDefault();
                alert('아이디와 비밀번호를 입력해주세요.');
            }
        });
    }

    // Add validation to business form
    if (businessForm) {
        businessForm.addEventListener('submit', function(e) {
            if (!validateForm(this)) {
                e.preventDefault();
                alert('아이디와 비밀번호를 입력해주세요.');
            }
        });
    }

    // Input field focus handling
    const inputFields = document.querySelectorAll('.input-field');
    inputFields.forEach(input => {
        input.addEventListener('focus', function() {
            this.style.borderColor = '#4a00e0';
        });

        input.addEventListener('blur', function() {
            if (this.value.trim() !== '') {
                this.style.borderColor = '#ddd';
            }
        });
    });

});
