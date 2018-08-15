package com.sea.seckill.exception;


import com.sea.seckill.result.CodeMsg;

public class GlobalException extends RuntimeException{

    private static final long serialVersionUID = 4167657879637407048L;

    private CodeMsg cm;

    public GlobalException(CodeMsg cm){
        super();
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }

}
