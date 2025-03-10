$(document).ready(function() {
    let selectedChoice;
    let userAnswers = {}; // 사용자의 답변을 저장하는 객체

    // 세션에 저장된 이전 답변이 있으면 가져오기
    loadPreviousAnswers();

    // 현재 문제에 대한 이전 답변이 있으면 선택 표시
    markPreviousAnswer();

    // 진행 상태바 업데이트
    updateProgressBar();

    $('.choice').on('click', function () {
        $('.choice').removeClass('selected');
        $(this).addClass('selected');

        selectedChoice = $(this).data('choice-key');

        // 현재 선택을 userAnswers에 저장
        userAnswers[currentQuestionNum] = selectedChoice;

        if (selectedChoice !== undefined) {
            $('#btn-next').prop('disabled', false);
        }
    });

    // 이전 버튼 클릭 이벤트
    $('#btn-prev').on('click', function() {
        if (currentQuestionNum > 1) {
            // 현재 선택을 저장
            if (selectedChoice) {
                userAnswers[currentQuestionNum] = selectedChoice;
            }

            // 이전 문제로 이동
            getPreviousQuestion(currentQuestionNum);
        }
    });

    // 다음 버튼 클릭 이벤트
    $('#btn-next').on('click', function() {
        if (selectedChoice) {
            submitAnswer(currentQuestionNum, selectedChoice);
        }
    });

    // 이전 문제를 가져오는 함수
    function getPreviousQuestion(currentNum) {
        $.ajax({
            url: root + "survey/previous",
            type: 'POST',
            data: {
                currentQuestionNum: currentNum
            },
            success: function(response) {
                currentQuestionNum--;
                updateQuestion(response.previousQuestion);
                updateProgressBar();
                updateQuestionNumber();

                // 이전/다음 버튼 상태 업데이트
                updateButtonsState();

                // 이전에 선택한 답변이 있으면 표시
                markPreviousAnswer();
            },
            error: function(xhr, status, error) {
                console.error('Error getting previous question:', error);
            }
        });
    }

    // 답변 제출 함수
    function submitAnswer(questionNum, answer) {
        // 현재 선택을 userAnswers에 저장
        userAnswers[questionNum] = answer;

        $.ajax({
            url: root + "survey/submit",
            type: 'POST',
            data: {
                questionNum: questionNum,
                answer: answer
            },
            success: function(response) {
                if (response.hasNext) {
                    // 다음 문제가 있는 경우
                    currentQuestionNum++;
                    updateQuestion(response.nextQuestion);
                    updateProgressBar();
                    updateQuestionNumber();

                    // 이전/다음 버튼 상태 업데이트
                    updateButtonsState();

                    // 이전에 선택한 답변이 있으면 표시
                    markPreviousAnswer();
                } else {
                    // 모든 문제를 다 푼 경우
                    window.location.href = response.redirectUrl;
                }
            },
            error: function(xhr, status, error) {
                console.error('Error submitting answer:', error);
            }
        });
    }

    // 질문 업데이트 함수
    function updateQuestion(question) {
        $('#question-text').text(question.question);

        // 선택지 초기화
        $('#choices-container').empty();

        // 새 선택지 생성
        Object.keys(question.choices).forEach(function(key) {
            let choice = question.choices[key];
            let choiceElement = $('<div>')
                .addClass('choice')
                .attr('data-choice-key', key)
                .attr('data-choice-value', choice.values);

            choiceElement.html('<strong>' + key + '.</strong> ' + choice.text);
            $('#choices-container').append(choiceElement);
        });

        // 이벤트 핸들러 재설정
        $('.choice').on('click', function() {
            $('.choice').removeClass('selected');
            $(this).addClass('selected');
            selectedChoice = $(this).data('choice-key');
            $('#btn-next').prop('disabled', false);
        });

        // 선택지 초기화
        selectedChoice = null;
        $('#btn-next').prop('disabled', true);
    }

    // 진행 상태바 업데이트 함수
    function updateProgressBar() {
        let progressPercentage = (currentQuestionNum / totalQuestions) * 100;
        $('#progress-bar').css('width', progressPercentage + '%');
    }

    // 문제 번호 업데이트 함수
    function updateQuestionNumber() {
        $('#current-question-num').text(currentQuestionNum);
    }

    // 이전/다음 버튼 상태 업데이트 함수
    function updateButtonsState() {
        // 첫 번째 문제일 경우 이전 버튼 비활성화
        if (currentQuestionNum > 1) {
            $('#btn-prev').prop('disabled', false);
        } else {
            $('#btn-prev').prop('disabled', true);
        }
    }

    // 이전에 저장된 답변 불러오기
    function loadPreviousAnswers() {
        $.ajax({
            url: root + "survey/getAnswers",
            type: 'GET',
            success: function(response) {
                if (response.answers) {
                    userAnswers = response.answers;
                    markPreviousAnswer();
                }
            },
            error: function(xhr, status, error) {
                console.error('Error loading previous answers:', error);
            }
        });
    }

    // 이전에 선택한 답변 표시하기
    function markPreviousAnswer() {
        if (userAnswers[currentQuestionNum]) {
            selectedChoice = userAnswers[currentQuestionNum];

            // 이전에 선택한 항목에 selected 클래스 추가
            $('.choice').each(function() {
                if ($(this).data('choice-key') === selectedChoice) {
                    $(this).addClass('selected');
                    $('#btn-next').prop('disabled', false);
                }
            });
        }
    }
});