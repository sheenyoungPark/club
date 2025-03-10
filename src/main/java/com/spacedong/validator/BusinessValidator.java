package com.spacedong.validator;

import com.spacedong.beans.BusinessBean;
import jakarta.validation.ConstraintViolation;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

public class BusinessValidator implements Validator {


    @Override
    public boolean supports(Class<?> clazz) {
        return BusinessBean.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BusinessBean businessBean = (BusinessBean) target;

    }
}


