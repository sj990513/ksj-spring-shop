package springshopksj.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import springshopksj.dto.DeliveryDto;
import springshopksj.dto.OrderDto;
import springshopksj.entity.Delivery;
import springshopksj.entity.Order;
import springshopksj.repository.DeliveryRepository;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ModelMapper modelMapper;

    private DeliveryDto convertToDeliveryDto(Delivery delivery) {
        DeliveryDto deliveryDto = modelMapper.map(delivery, DeliveryDto.class);
        deliveryDto.setOrderID(delivery.getOrder().getID());
        return deliveryDto;
    }


    //모든 order 조회 - 페이징처리
    public Page<DeliveryDto> findAllDelivery(Pageable pageable) {

        Page<Delivery> allDelivery = deliveryRepository.findAll(pageable);

        return allDelivery.map(this::convertToDeliveryDto);
    }

    // delivery status별 조회 - 페이징 처리
    public Page<DeliveryDto> findByStatus(String status, Pageable pageable) {

        Delivery.DeliveryStatus deliveryStatus;

        switch (status) {
            case "ready":
                deliveryStatus = Delivery.DeliveryStatus.READY;
                break;

            case "shipped":
                deliveryStatus = Delivery.DeliveryStatus.SHIPPED;
                break;

            case "delivered":
                deliveryStatus = Delivery.DeliveryStatus.DELIVERED;
                break;

            case "cancelled":
                deliveryStatus = Delivery.DeliveryStatus.CANCELLED;
                break;

            default:
                deliveryStatus = Delivery.DeliveryStatus.READY;
        }

        Page<Delivery> findByCategory = deliveryRepository.findByStatus(deliveryStatus, pageable);

        return findByCategory.map(this::convertToDeliveryDto);
    }

    // 특정 배달정보 조회 (관리자페이지에서 사용)
    public DeliveryDto getDeliveryById(Long deliveryId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        DeliveryDto deliveryDto = convertToDeliveryDto(delivery);

        return  deliveryDto;
    }

    // 배송 status 변경
    public void updateDeliveryStatus(long deliveryId, Delivery.DeliveryStatus status) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("해당하는 배송정보를 찾을수 없습니다."));

        delivery.setStatus(status);

        deliveryRepository.save(delivery);
    }
}
