package sharing.app.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sharing.app.com.config.MapperConfig;
import sharing.app.com.dto.rental.RentalDto;
import sharing.app.com.model.Rental;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "user.id", target = "userId")
    RentalDto toDto(Rental rental);

    Rental toModel(RentalDto rentalDto);
}
