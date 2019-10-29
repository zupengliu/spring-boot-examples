package com.soil.completable.future.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {

    private long id;

    private String userName;

    private int age;

    private int sex;

}
