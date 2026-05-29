// 页面加载完成后执行
$(document).ready(function() {
    // 初始化登录验证码
    refreshLoginCaptcha();

    // 页面加载时检查是否有记住密码
    var savedUsername = localStorage.getItem('savedUsername');
    var savedPassword = localStorage.getItem('savedPassword');
    var rememberMe = localStorage.getItem('rememberMe');

    if (savedUsername) {
        $('#login-username').val(savedUsername);
    }
    if (rememberMe === 'true' && savedPassword) {
        $('#login-password').val(savedPassword);
        $('#rememberMe').prop('checked', true);
    }
});

// 切换登录密码显示/隐藏
function toggleLoginPassword() {
    var passwordInput = $('#login-password');
    var eyeIcon = $('#toggleLoginPassword');
    var type = passwordInput.attr('type') === 'password' ? 'text' : 'password';
    passwordInput.attr('type', type);
    eyeIcon.text(type === 'password' ? '👁️' : '🙈');
}

// 切换注册密码显示/隐藏
function toggleRegPassword() {
    var passwordInput = $('#reg-password');
    var eyeIcon = $('#toggleRegPassword');
    var type = passwordInput.attr('type') === 'password' ? 'text' : 'password';
    passwordInput.attr('type', type);
    eyeIcon.text(type === 'password' ? '👁️' : '🙈');
}

// 切换注册确认密码显示/隐藏
function toggleRegConfirmPassword() {
    var passwordInput = $('#reg-confirm-password');
    var eyeIcon = $('#toggleRegConfirmPassword');
    var type = passwordInput.attr('type') === 'password' ? 'text' : 'password';
    passwordInput.attr('type', type);
    eyeIcon.text(type === 'password' ? '👁️' : '🙈');
}

// 刷新登录验证码
function refreshLoginCaptcha() {
    document.getElementById('loginCaptchaImg').src = '/captcha/image?' + Math.random();
}

// 刷新注册验证码
function refreshRegCaptcha() {
    document.getElementById('regCaptchaImg').src = '/captcha/image?' + Math.random();
}

// 显示用户协议
function showProtocol() {
    alert('用户协议\n\n1. 欢迎使用本系统\n2. 用户需遵守当地法律法规\n3. 用户不得利用本系统从事违法活动\n4. 用户的个人信息将受到保护');
}

// 显示隐私政策
function showPrivacy() {
    alert('隐私政策\n\n1. 我们收集您的基本信息用于提供服务\n2. 您的信息不会被出售给第三方\n3. 我们采取安全措施保护您的数据\n4. 您可以随时查看和删除您的数据');
}

// 登录功能
function doLogin() {
    var username = $('#login-username').val();
    var password = $('#login-password').val();
    var captcha = $('#login-captcha').val();
    var rememberMe = $('#rememberMe').is(':checked');

    if (!username || !password) {
        $('#login-error').text('请填写用户名和密码!').show();
        return;
    }

    $('#login-error').hide();
    $('#loginBtn').text('登录中...').prop('disabled', true);

    $.ajax({
        url: '/user/login',
        type: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify({
            username: username,
            password: password,
            captcha: captcha || '',
            rememberMe: rememberMe
        }),
        success: function(response) {
                console.log('登录响应:', response);
                if (response.code === 200) {
                    // 处理记住密码
                    if (rememberMe) {
                        localStorage.setItem('savedUsername', username);
                        localStorage.setItem('savedPassword', password);
                        localStorage.setItem('rememberMe', 'true');
                    } else {
                        localStorage.removeItem('savedPassword');
                        localStorage.setItem('savedUsername', username);
                        localStorage.setItem('rememberMe', 'false');
                    }

                    // 保存用户信息到localStorage，扁平化数据结构
                    var userData = response.data;
                    var currentUser = {
                        id: userData.user.userid,
                        username: userData.user.username,
                        nickname: userData.user.nickname,
                        avatar: userData.user.avatar,
                        role: userData.user.role,
                        name: userData.profile ? userData.profile.name : username,
                        phone: userData.profile ? userData.profile.phone : '',
                        email: userData.profile ? userData.profile.email : ''
                    };
                    localStorage.setItem('currentUser', JSON.stringify(currentUser));

//                    alert('登录成功！');
                    // 跳转到主页，携带用户信息
                    window.location.href = '/generator/index.html';
            } else {
                $('#login-error').text(response.msg || '登录失败').show();
                refreshLoginCaptcha();
            }
        },
        error: function(xhr, status, error) {
            console.log('登录错误:', xhr, status, error);
            var response = xhr.responseJSON || {msg: '登录失败，请稍后重试'};
            $('#login-error').text(response.msg || '登录失败，请稍后重试').show();
            refreshLoginCaptcha();
        },
        complete: function() {
            $('#loginBtn').text('登录').prop('disabled', false);
        }
    });
}

// 注册功能
function doRegister() {
    var username = $('#reg-username').val();
    var password = $('#reg-password').val();
    var confirmPassword = $('#reg-confirm-password').val();
    var captcha = $('#reg-captcha').val();
    var agreeProtocol = $('#agreeProtocol').is(':checked');

    if (!username || !password || !confirmPassword || !captcha) {
        $('#register-error').text('请填写完整信息!').show();
        return;
    }

    if (!agreeProtocol) {
        $('#register-error').text('请先阅读并同意用户协议和隐私政策!').show();
        return;
    }

    if (password !== confirmPassword) {
        $('#register-error').text('两次输入的密码不一致!').show();
        return;
    }

    $('#register-error').hide();
    $('#registerBtn').text('注册中...').prop('disabled', true);

    $.ajax({
        url: '/user/register',
        type: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify({
            username: username,
            password: password,
            confirmPassword: confirmPassword,
            captcha: captcha
        }),
        success: function(response) {
            console.log('注册响应:', response);
            if (response.code === 200) {
                alert('注册成功！即将跳转到登录页面...');
                // 清空表单
                $('#reg-username').val('');
                $('#reg-password').val('');
                $('#reg-confirm-password').val('');
                $('#reg-captcha').val('');
                $('#agreeProtocol').prop('checked', false);
                // 跳转到登录页
                window.location.href = '../login/index.html';
            } else {
                $('#register-error').text(response.msg || '注册失败!').show();
                refreshRegCaptcha();
            }
        },
        error: function(xhr, status, error) {
            console.log('注册错误:', xhr, status, error);
            var response = xhr.responseJSON || {msg: '注册失败，请稍后重试!'};
            $('#register-error').text(response.msg || '注册失败，请稍后重试!').show();
            refreshRegCaptcha();
        },
        complete: function() {
            $('#registerBtn').text('注册').prop('disabled', false);
        }
    });
}