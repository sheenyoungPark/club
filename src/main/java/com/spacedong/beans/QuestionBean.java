package com.spacedong.beans;

import java.util.Map;

public class QuestionBean {
	
	private int num;
    private String question;
    private Map<String, ChoiceBean> choices;
    
    public int getNum() {
        return num;
    }
    
    public void setNum(int num) {
        this.num = num;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public Map<String, ChoiceBean> getChoices() {
        return choices;
    }
    
    public void setChoices(Map<String, ChoiceBean> choices) {
        this.choices = choices;
    }

}
