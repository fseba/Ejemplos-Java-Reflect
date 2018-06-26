package dto;

import lombok.Data;

import java.sql.Date;

@Data
public class Factura {
    private Integer numero;
    private Date fecha;
}
