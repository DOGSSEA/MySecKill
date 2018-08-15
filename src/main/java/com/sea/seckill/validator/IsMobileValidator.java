package com.sea.seckill.validator;

import com.sea.seckill.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidator implements ConstraintValidator<IsMobile , String> {
   private boolean required = false;

    @Override
    public void initialize(IsMobile isMobile) {
       required = isMobile.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(!required){
            if(StringUtils.isEmpty(s)){
                return true;
            }
        }
        return ValidatorUtil.isMobile(s);
    }
}
