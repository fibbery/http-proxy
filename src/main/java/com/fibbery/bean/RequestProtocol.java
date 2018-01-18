package com.fibbery.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author fibbery
 * @date 18/1/17
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestProtocol implements Serializable {

    private static final long serialVersionUID = 7910714866238377300L;

    private String host;

    private int port;

    private boolean isSSL;

}
