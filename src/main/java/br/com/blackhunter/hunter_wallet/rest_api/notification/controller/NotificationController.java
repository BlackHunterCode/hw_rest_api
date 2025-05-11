package br.com.blackhunter.hunter_wallet.rest_api.notification.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.blackhunter.hunter_wallet.rest_api.core.controller.BaseController;
import br.com.blackhunter.hunter_wallet.rest_api.core.dto.ApiResponse;
import br.com.blackhunter.hunter_wallet.rest_api.notification.entity.NotificationEntity;

@RestController
@RequestMapping("/notification")
public class NotificationController extends BaseController<NotificationEntity, UUID>{

    @Override
    public ResponseEntity<ApiResponse<NotificationEntity>> findById(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Iterable<NotificationEntity>>> findAll() {
        List<NotificationEntity> salve = new ArrayList<NotificationEntity>();
        salve.add(new NotificationEntity());
        return ok(salve);
    }

    @Override
    public ResponseEntity<ApiResponse<NotificationEntity>> create(NotificationEntity payload) {
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public ResponseEntity<ApiResponse<NotificationEntity>> update(UUID id, NotificationEntity payload) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> delete(UUID id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> existsById(UUID id) {
        throw new UnsupportedOperationException("Unimplemented method 'existsById'");
    }

    @Override
    public ResponseEntity<ApiResponse<Long>> count() {
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

}
