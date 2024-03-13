package io.github.semanticpie.derezhor.common.services;

import lombok.AllArgsConstructor;
import org.ostis.api.context.DefaultScContext;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JmanticService {

    private final DefaultScContext context;
}

//Egor: с одной стороны сервис сделан для инкапсулирования контекста, но
// теперь нам придется сначала просить Jmantic, потом получать контекст и дальше с ним работать,
// не уверен что хорошая идея увеличивать код, мы и так уже привыкли обращаться к памяти через контекст