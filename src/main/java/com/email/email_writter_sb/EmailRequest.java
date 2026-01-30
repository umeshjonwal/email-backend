package com.email.email_writter_sb;

import lombok.Data;

@Data
public class EmailRequest {

    private String emailContent;
    private String tone;
}
