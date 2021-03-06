package com.rtr.rotatingArm;


public class Stack {

    private Mat4[] _stack;
    private int sp;

    public Stack() {
        _stack = new Mat4[10];
        sp = -1;
    }

    public void resetStack() {
        System.out.println("RTR: STACK RESET\n");
        sp = -1;
    }

    public void push(Mat4 data) {
        if (sp == 10 - 1)
        {
            System.out.println("RTR: STACK OVERFLOW\n");
        }
        else
        {
            sp++;
            _stack[sp] = data;
        }
    }

    public Mat4 pop() {
        Mat4 data = new Mat4();
        if (sp > -1)
        {
            data = _stack[sp];
            sp--;
        }
        else
        {
            System.out.println("RTR: STACK UNDERFLOW\n");
        }
        System.out.println("RTR: STACK POP: \n");
        
        return data;
    }

    Mat4 peek() {
        if (sp > -1) {
            System.out.println("RTR: STACK PEEK: \n");
            Mat4 data = _stack[sp];
            
            return _stack[sp];
        }

        return new Mat4();
    }


}