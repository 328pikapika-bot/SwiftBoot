<template>
  <div class="login-container">
    <div class="visual-side">
      <div class="mesh-gradient"></div>
      <div class="brand-showcase">
        <div class="logo-large">
          <SwiftLogo class="logo-icon-large" />
        </div>
        <h1 class="brand-title">SwiftBoot</h1>
        <p class="brand-slogan">{{ $t('login.brandSlogan') }}</p>
      </div>
    </div>
    
    <div class="form-side">
      <div class="login-lang-switch">
        <el-dropdown trigger="click" @command="handleLanguageChange">
          <div class="lang-btn">
            <span>{{ locale === 'zh-cn' ? '中文' : 'En' }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh-cn" :disabled="locale === 'zh-cn'">中文</el-dropdown-item>
              <el-dropdown-item command="en" :disabled="locale === 'en'">English</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div class="form-wrapper">
        <div class="form-header">
          <h2>{{ $t('login.title') }}</h2>
          <p>{{ $t('login.subtitle') }}</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <div class="input-label">{{ $t('login.username') }}</div>
            <el-input
              v-model="loginForm.username"
              :placeholder="$t('login.usernamePlaceholder')"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-label">{{ $t('login.password') }}</div>
            <el-input
              v-model="loginForm.password"
              type="password"
              :placeholder="$t('login.passwordPlaceholder')"
              show-password
              class="custom-input"
            />
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">{{ $t('login.rememberMe') }}</el-checkbox>
            <a href="javascript:;" class="forgot-password">{{ $t('login.forgotPassword') }}</a>
          </div>
          
          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="handleLogin"
          >
            {{ loading ? $t('login.loggingIn') : $t('login.login') }}
          </el-button>
          
          <div class="social-login">
            <div class="divider"><span>{{ $t('login.orContinueWith') }}</span></div>
            <div class="social-icons">
              <button class="social-btn">
                <el-icon><Platform /></el-icon>
              </button>
              <button class="social-btn">
                <el-icon><ChromeFilled /></el-icon>
              </button>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import SwiftLogo from '@/components/SwiftLogo/index.vue'
import { Platform, ChromeFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t, locale } = useI18n()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const loginRules = computed<FormRules>(() => ({
  username: [{ required: true, message: t('login.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.passwordRequired'), trigger: 'blur' }]
}))

const handleLanguageChange = (lang: string) => {
  locale.value = lang
  localStorage.setItem('language', lang)
}

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success(t('login.success'))
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (error: any) {
    ElMessage.error(error.message || t('login.failed'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const savedUsername = localStorage.getItem('rememberedUsername')
  const savedPassword = localStorage.getItem('rememberedPassword')
  if (savedUsername && savedPassword) {
    loginForm.username = savedUsername
    loginForm.password = savedPassword
    rememberMe.value = true
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  font-family: 'Outfit', sans-serif;
  background: #fff;
}

.visual-side {
  width: 50%;
  position: relative;
  background: radial-gradient(120% 120% at 0% 0%, #1e293b 0%, #0f172a 45%, #0b1020 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  
  .mesh-gradient {
    position: absolute;
    width: 150%;
    height: 150%;
    background: 
      radial-gradient(at 15% 20%, rgba(59, 130, 246, 0.35) 0, transparent 55%), 
      radial-gradient(at 65% 15%, rgba(129, 140, 248, 0.3) 0, transparent 50%), 
      radial-gradient(at 85% 70%, rgba(14, 165, 233, 0.25) 0, transparent 55%);
    filter: blur(90px);
    opacity: 0.9;
    animation: meshMove 24s infinite alternate ease-in-out;
  }
  
  .brand-showcase {
    position: relative;
    z-index: 10;
    text-align: center;
    color: white;
    
    .logo-large {
      margin-bottom: 2.25rem;
      transform: scale(2.6);
      filter: drop-shadow(0 18px 35px rgba(15, 23, 42, 0.55));
    }
    
    .brand-title {
      font-size: 3.8rem;
      font-weight: 700;
      letter-spacing: -1px;
      margin-bottom: 1.1rem;
      background: linear-gradient(135deg, #ffffff 0%, #c7d2fe 45%, #93c5fd 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      text-shadow: 0 20px 40px rgba(15, 23, 42, 0.5);
    }
    
    .brand-slogan {
      font-size: 1.15rem;
      color: rgba(226, 232, 240, 0.75);
      font-weight: 300;
      letter-spacing: 0.6px;
    }
  }
}

@keyframes meshMove {
  0% { transform: translate(-10%, -10%) rotate(0deg); }
  100% { transform: translate(10%, 10%) rotate(5deg); }
}

.form-side {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  position: relative;
  
  .form-wrapper {
    width: 100%;
    max-width: 420px;
    padding: 2.75rem 2.5rem;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 24px;
    border: 1px solid rgba(226, 232, 240, 0.8);
    box-shadow: 
      0 30px 60px rgba(15, 23, 42, 0.12),
      0 8px 20px rgba(15, 23, 42, 0.06);
    backdrop-filter: blur(8px);
  }
}

.login-lang-switch {
  position: absolute;
  top: 28px;
  right: 36px;
  z-index: 10;
}

.lang-btn {
  height: 36px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.9);
  color: #475569;
  font-size: 0.875rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: #fff;
    color: #1e293b;
    border-color: rgba(148, 163, 184, 0.6);
  }
}

.form-header {
  margin-bottom: 2.75rem;
  
  h2 {
    font-size: 2.1rem;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 0.5rem;
    letter-spacing: -0.5px;
  }
  
  p {
    color: #64748b;
    font-size: 1rem;
  }
}

.input-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #334155;
  margin-bottom: 0.5rem;
}

:deep(.custom-input) {
  .el-input__wrapper {
    background-color: #f8fafc;
    border: 1px solid rgba(148, 163, 184, 0.3);
    box-shadow: none !important;
    border-radius: 14px;
    padding: 12px 16px;
    height: 48px;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: rgba(99, 102, 241, 0.35);
      background-color: #f1f5f9;
    }
    
    &.is-focus {
      border-color: rgba(59, 130, 246, 0.55);
      background-color: #fff;
      box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.12) !important;
    }
    
    .el-input__inner {
      font-weight: 500;
      color: #1e293b;
      
      &::placeholder {
        color: #94a3b8;
      }
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.2rem;
  margin-top: 0.5rem;
  
  :deep(.el-checkbox__label) {
    color: #64748b;
  }
  
  .forgot-password {
    color: #409eff;
    font-size: 0.875rem;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.submit-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.35);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 28px rgba(37, 99, 235, 0.45);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.social-login {
  margin-top: 2.25rem;
  
  .divider {
    position: relative;
    text-align: center;
    margin-bottom: 1.5rem;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      width: 100%;
      height: 1px;
      background: #e2e8f0;
    }
    
    span {
      position: relative;
      background: #fff;
      padding: 0 1rem;
      color: #94a3b8;
      font-size: 0.875rem;
    }
  }
  
  .social-icons {
    display: flex;
    justify-content: center;
    gap: 1rem;
    
    .social-btn {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      background: #fff;
      color: #64748b;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
      transition: all 0.2s;
      
      &:hover {
        border-color: #cbd5e1;
        background: #f8fafc;
        color: #1e293b;
      }
    }
  }
}

@media (max-width: 900px) {
  .visual-side {
    display: none;
  }
  .form-side {
    width: 100%;
  }
}
</style>
