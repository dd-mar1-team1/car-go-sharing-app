package sharing.app.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sharing.app.com.config.MapperConfig;
import sharing.app.com.dto.car.CarDto;
import sharing.app.com.dto.car.CreateCarRequestDto;
import sharing.app.com.model.Car;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toModel(CreateCarRequestDto requestDto);

    void updateCarFromDto(CreateCarRequestDto requestDto, @MappingTarget Car car);
}
