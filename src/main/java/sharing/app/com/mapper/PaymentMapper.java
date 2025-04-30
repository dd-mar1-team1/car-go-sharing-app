package sharing.app.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sharing.app.com.config.MapperConfig;
import sharing.app.com.dto.payment.PaymentDto;
import sharing.app.com.model.Payment;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(source = "rental.id", target = "rentalId")
    PaymentDto toDto(Payment payment);
}
