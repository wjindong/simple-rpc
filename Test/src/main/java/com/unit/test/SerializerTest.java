package com.unit.test;

import com.simple.rpc.serializer.protostuff.ProtostuffSerializer;
import com.simple.rpc.util.StringUtil;

class T{
    int a;
    T(int a){}
}

class Student{
    public String name ;
    public int age;
    public T t;
    Student(){}
    Student(String name,int age,T t){
        this.name=name;
        this.age=age;
        this.t=t;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", t=" + t +
                '}';
    }
}

public class SerializerTest {
    public static void main(String[] args) {
        ProtostuffSerializer protostuffSerializer=new ProtostuffSerializer();
        Student student=new Student("wangjindong",23,new T(4));
        byte[] bytes=protostuffSerializer.serialize(student);
        System.out.println(new String(bytes));
        Student obj=protostuffSerializer.deserialize(bytes,Student.class);
        System.out.println(obj.name+","+obj.age);
        System.out.println("================ json test==============");

        System.out.println(StringUtil.ObjectToJson(student));
        System.out.println(StringUtil.JsonToObject(StringUtil.ObjectToJson(student),student.getClass()));
    }
}
