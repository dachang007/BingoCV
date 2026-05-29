import { useUserStore } from "@/store/user.js";
import { useSettingStore } from "@/store/setting.js";
import { loginUserInfo } from "@/request/my.js";
import { websiteConfig } from "@/request/setting.js";
import i18n from "@/i18n/index.js";

export async function init() {
  document.title = '\u200B';

  const settingStore = useSettingStore();
  const userStore = useUserStore();
  const token = localStorage.getItem('token');

  initLanguage(settingStore);

  const setting = await websiteConfig();
  settingStore.settings = setting;
  settingStore.domainList = setting.domainList;
  document.title = setting.title || 'BingoCV';

  if (token) {
    const user = await loginUserInfo().catch(() => null);
    if (user) {
      userStore.user = user;
    }
  }
}

export async function initBingoCV() {
  document.title = 'BingoCV';

  const settingStore = useSettingStore();
  const userStore = useUserStore();
  initLanguage(settingStore);

  if (!settingStore.settings.title) {
    settingStore.settings.title = 'BingoCV';
  }

  const userInfo = localStorage.getItem('userInfo');
  if (userInfo) {
    try {
      const decodedStr = decodeURIComponent(escape(atob(userInfo)));
      userStore.user = JSON.parse(decodedStr);
    } catch (e) {
      console.error('解析用户信息失败', e);
      localStorage.removeItem('userInfo');
    }
  }
}

function initLanguage(settingStore) {
  if (!settingStore.lang) {
    const lang = navigator.language.split('-')[0];
    settingStore.lang = lang === 'zh' ? 'zh' : 'en';
  }
  i18n.global.locale.value = settingStore.lang;
}
