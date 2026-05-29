/**
 * BingoCV 生成页 - 原生 jQuery 实现（无 Vue 依赖）
 */
//var API_BASE = '/v1';

// ===== 状态变量 =====
var currentUser = null;    // 当前登录用户
var edus = [];              // 暂存的教育经历列表
var works = [];             // 暂存的工作经历列表
var selectedSex = '';       // 选中的性别

// ===== 工具函数：读取表单值 =====
function val(id) { return $('#' + id).val() || ''; }
function setVal(id, v) { $('#' + id).val(v || ''); }

// ===== 工具函数：日期校验 =====
function validateDateRange(startId, endId, startLabel, endLabel) {
    var start = val(startId);
    var end = val(endId);
    if (start && end) {
        if (new Date(start) > new Date(end)) {
            alert(startLabel + '不能大于' + endLabel);
            return false;
        }
    }
    return true;
}

// ===== 性别选择 =====
function setSex(sex) {
    selectedSex = sex;
}

// ===== 暂存教育经历 =====
function saveEdu() {
    var school = val('edu-school');
    if (!school) { alert('请填写学校名称'); return; }
    if (!validateDateRange('edu-start', 'edu-end', '入学日期', '毕业日期')) {
        return;
    }
    edus.push({
        school: school, study: val('edu-study'), start: val('edu-start'),
        end: val('edu-end'), description: val('edu-description')
    });
    clearEduForm();
    alert('已暂存教育经历（共' + edus.length + '条）');
}

function eduClick() {
    if (val('edu-school')) {
        if (!validateDateRange('edu-start', 'edu-end', '入学日期', '毕业日期')) {
            return;
        }
        edus.push({
            school: val('edu-school'), study: val('edu-study'), start: val('edu-start'),
            end: val('edu-end'), description: val('edu-description')
        });
        clearEduForm();
    }
}

function clearEduForm() {
    setVal('edu-school', ''); setVal('edu-study', ''); setVal('edu-start', '');
    setVal('edu-end', ''); setVal('edu-description', '');
}

// ===== 暂存工作经历 =====
function saveWork() {
    var company = val('work-company');
    if (!company) { alert('请填写公司名称'); return; }
    if (!validateDateRange('work-start', 'work-end', '开始日期', '结束日期')) {
        return;
    }
    works.push({
        company: company, job: val('work-job'), start: val('work-start'),
        end: val('work-end'), description: val('work-description')
    });
    clearWorkForm();
    alert('已暂存工作经历（共' + works.length + '条）');
}

function workClick() {
    if (val('work-company')) {
        if (!validateDateRange('work-start', 'work-end', '开始日期', '结束日期')) {
            return;
        }
        works.push({
            company: val('work-company'), job: val('work-job'), start: val('work-start'),
            end: val('work-end'), description: val('work-description')
        });
        clearWorkForm();
    }
}

function clearWorkForm() {
    setVal('work-company', ''); setVal('work-job', ''); setVal('work-start', '');
    setVal('work-end', ''); setVal('work-description', '');
}

// ===== 收集所有表单数据 =====
function collectFormData() {
    var profiles = {
        name: val('user-name'), age: val('user-age'), city: val('user-city'),
        address: val('user-address'), email: val('user-email'), phone: val('user-phone'),
        weixin: val('user-weixin'), qq: val('user-qq'), weibo: val('user-weibo'),
        sex: selectedSex, description: val('user-description')
    };
    // 如果已登录，带上 userid
    if (currentUser && currentUser.id) {
        profiles.userid = currentUser.id;
    }

    var specialtyList = [];
    if (val('spec1-name')) {
        specialtyList.push({ name: val('spec1-name'), description: val('spec1-desc') });
    }
    if (val('spec2-name')) {
        specialtyList.push({ name: val('spec2-name'), description: val('spec2-desc') });
    }

    return {
        profiles: profiles, eduList: edus, workList: works,
        skill: { keywords: val('skill-keywords') }, specialtyList: specialtyList
    };
}

// ===== 最终统一提交 =====
function submitClick() {
    var data = collectFormData();
    $('#result-title').text('正在生成主页...');
    $('#result-heading').text('正在生成主页...');
    $('#result-content').text('提交数据中...');

    // 构建请求头，添加用户信息
    var headers = { 
        'Content-Type': 'application/json' 
    };
    if (currentUser) {
        // 将用户信息转换为 Base64 编码
        var userInfo = JSON.stringify({
            id: currentUser.id,
            username: currentUser.username,
            nickname: currentUser.nickname,
            avatar: currentUser.avatar,
            roles: currentUser.role ? [currentUser.role] : []
        });
        // 使用 UTF-8 编码处理中文字符
        headers['USER-INFO'] = btoa(unescape(encodeURIComponent(userInfo)));
        console.log('发送的用户信息:', userInfo);
        console.log('发送的 USER-INFO 头:', headers['USER-INFO']);
    }

//    fetch(API_BASE + '/info/submit', {
    fetch('/info/submit', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(data)
    })
    .then(function(res) { return res.json(); })
    .then(function(json) {
        if (json.code === 200) {
            $('#result-title').text('生成成功：');
            $('#result-heading').text('生成成功：');
            $('#result-content').html('访问地址：<a href="../profile/u.html?userid=' + json.data + '">点击访问</a>');
        } else {
            $('#result-title').text('生成失败');
            $('#result-heading').text('生成失败');
            $('#result-content').text(json.msg || '提交失败，请重试');
        }
    })
    .catch(function() {
        $('#result-title').text('生成失败');
        $('#result-heading').text('生成失败');
        $('#result-content').text('网络错误，请重试');
    });
}

// ===== 退出登录 =====
function logout() {
    currentUser = null;
    localStorage.removeItem('currentUser');
    updateTopBar();
    resetForm();
    alert('已退出登录');
}

// ===== 更新顶部栏状态 =====
function updateTopBar() {
    if (currentUser) {
        $('#user-info-area').css('display', 'block');
        $('#user-info-area').addClass('open');
        $('#login-btn-area').hide();
        if (currentUser.avatar) {
            $('#user-avatar').attr('src', currentUser.avatar).show();
        } else {
            $('#user-avatar').hide();
        }
        $('#user-name-display').text(currentUser.name || currentUser.username);
    } else {
        $('#user-info-area').css('display', 'none');
        $('#user-info-area').removeClass('open');
        $('#login-btn-area').show();
    }
}

// ===== 下拉菜单点击事件 =====
$(function() {
    // 恢复登录状态
    var saved = localStorage.getItem('currentUser');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
            updateTopBar();
            loadExistingData();
        } catch(e) {
            localStorage.removeItem('currentUser');
        }
    }

    // 点击空白处关闭下拉菜单
    $(document).on('click', function(e) {
        if (!$(e.target).closest('.dropdown').length) {
            $('.dropdown').removeClass('open');
        }
    });

    // 下拉菜单切换
    $('.user-dropdown-toggle').on('click', function(e) {
        e.stopPropagation();
        $(this).parent().toggleClass('open');
    });
});

// ===== 显示个人信息 =====
function showProfile() {
    if (!currentUser) {
        alert('请先登录');
        return;
    }
    var info = '用户名: ' + (currentUser.username || '') + '\n';
    info += '昵称: ' + (currentUser.nickname || '未设置') + '\n';
    info += '姓名: ' + (currentUser.name || '未设置') + '\n';
    info += '电话: ' + (currentUser.phone || '未设置') + '\n';
    info += '邮箱: ' + (currentUser.email || '未设置') + '\n';
    info += '角色: ' + (currentUser.role || '');
    alert(info);
}

// ===== 修改密码 =====
function editPassword() {
    if (!currentUser) {
        alert('请先登录');
        return;
    }
    alert('修改密码功能开发中...');
}

// ===== 退出登录 =====
function logout() {
    currentUser = null;
    localStorage.removeItem('currentUser');
    updateTopBar();
    resetForm();
    alert('已退出登录');
}

// ===== 加载已有数据（登录后调用）=====
function loadExistingData() {
    if (!currentUser || !currentUser.id) return;
    
    var headers = { 'Content-Type': 'application/json' };
    var userInfo = JSON.stringify({
        id: currentUser.id,
        username: currentUser.username,
        nickname: currentUser.nickname,
        avatar: currentUser.avatar,
        roles: currentUser.role ? [currentUser.role] : []
    });
    // 使用 UTF-8 编码处理中文字符
    headers['USER-INFO'] = btoa(unescape(encodeURIComponent(userInfo)));
    console.log('发送的用户信息:', userInfo);
    console.log('发送的 USER-INFO 头:', headers['USER-INFO']);
    
//    fetch(API_BASE + '/info?userid=' + currentUser.id, { headers: headers })
    fetch('/info?userid=' + currentUser.id, { headers: headers })
    .then(function(res) { return res.json(); })
    .then(function(json) {
        if (json.code === 200 && json.data && json.data.user) {
            var info = json.data;
            // 预填用户信息
            setVal('user-name', info.user.name);
            setVal('user-age', info.user.age);
            setVal('user-city', info.user.city);
            setVal('user-address', info.user.address);
            setVal('user-email', info.user.email);
            setVal('user-phone', info.user.phone);
            setVal('user-weixin', info.user.weixin);
            setVal('user-qq', info.user.qq);
            setVal('user-weibo', info.user.weibo);
            setVal('user-description', info.user.description);
            selectedSex = info.user.sex || '';
            if (selectedSex === 'nan') {
                $('#sex1').prop('checked', true).parent().addClass('active');
            } else if (selectedSex === 'nv') {
                $('#sex2').prop('checked', true).parent().addClass('active');
            }
            // 预填教育经历
            if (info.eduList && info.eduList.length > 0) {
                edus = info.eduList.slice(1);
                var first = info.eduList[0];
                setVal('edu-school', first.school);
                setVal('edu-study', first.study);
                setVal('edu-start', first.start);
                setVal('edu-end', first.end);
                setVal('edu-description', first.description);
            }
            // 预填工作经历
            if (info.workList && info.workList.length > 0) {
                works = info.workList.slice(1);
                var firstW = info.workList[0];
                setVal('work-company', firstW.company);
                setVal('work-job', firstW.job);
                setVal('work-start', firstW.start);
                setVal('work-end', firstW.end);
                setVal('work-description', firstW.description);
            }
            // 预填技能
            if (info.skill) {
                setVal('skill-keywords', info.skill.keywords);
            }
            // 预填特长
            if (info.specialtyList && info.specialtyList.length > 0) {
                if (info.specialtyList[0]) {
                    setVal('spec1-name', info.specialtyList[0].name);
                    setVal('spec1-desc', info.specialtyList[0].description);
                }
                if (info.specialtyList[1]) {
                    setVal('spec2-name', info.specialtyList[1].name);
                    setVal('spec2-desc', info.specialtyList[1].description);
                }
            }
            alert('已加载已有简历数据');
        }
    })
    .catch(function(e) {
        console.error('加载已有数据失败', e);
    });
}

// ===== 重置表单 =====
function resetForm() {
    setVal('user-name', ''); setVal('user-age', ''); setVal('user-city', '');
    setVal('user-address', ''); setVal('user-email', ''); setVal('user-phone', '');
    setVal('user-weixin', ''); setVal('user-qq', ''); setVal('user-weibo', '');
    setVal('user-description', '');
    selectedSex = '';
    $('#sex1, #sex2').prop('checked', false).parent().removeClass('active');
    clearEduForm(); clearWorkForm();
    setVal('skill-keywords', '');
    setVal('spec1-name', ''); setVal('spec1-desc', '');
    setVal('spec2-name', ''); setVal('spec2-desc', '');
    edus = []; works = [];
}
