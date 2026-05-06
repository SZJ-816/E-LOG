package cn.edu.tjrac.vo;

import lombok.Data;

@Data
public class ErrorLogVO {
    private Long id;
    private String timestamp;
    private String level;
    private String service;
    private String message;
}
