/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2018 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.mikroskeem.orion.core.extensions;

import eu.mikroskeem.orion.core.OrionCore;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IEnvironmentTokenProvider;


/**
 * Provides <pre>CB_VERSION</pre> token for {@link org.spongepowered.asm.mixin.Overwrite} constraint
 *
 * See <a href="https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---Overwriting-Methods#3-constraints">constraints</a>
 *
 * @author Mark Vainomaa
 */
public final class OrionTokenProvider implements IEnvironmentTokenProvider {
    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Integer getToken(String token, MixinEnvironment env) {
        if(token.equals("CB_VERSION"))
            return OrionCore.INSTANCE.getCBVersion().getId();

        return null;
    }
}
