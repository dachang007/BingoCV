(function ($) {
    $.getUrlParam = function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }
})(jQuery);

$(function () {
    const userid = $.getUrlParam("userid");
    if (userid != null) {
        $.getJSON("/info", { userid: userid }, function (res) {
            if (res.code !== 200 || res.data == null) {
                layer.msg("用户编号不存在，生成主页失败");
                return;
            }
            const data = res.data;
            const user = data.profiles || data.user;
            const skill = data.skill;
            const edus = data.eduList || [];
            const works = data.workList || [];
            const specialtys = data.specialtyList || [];

            changeUserText(user);
            changeUserAddr(user);
            changeSkill(skill);
            changeEdu(edus);
            changeWork(works);
            changeSpecialtys(specialtys);
        });
    } else {
        layer.msg("参数错误，请检查网址");
    }
});

function changeUserText(user) {
    $(".user-name").text(user.name);
    $(".user-age").text(user.age);
    $(".user-city").text(user.city);
    $(".user-phone").text(user.phone);
    $(".user-email").text(user.email);
    $(".user-weixin").text(user.weixin);
    $(".user-address").text(user.address);
    $(".user-description").text(user.description);
}

function changeUserAddr(user) {
    $(".user-name,.user-name-alt").attr("title", user.name);
    $(".user-weibo-href").attr("href", user.weibo);
    $(".user-weibo-href").attr("title", user.weibo);
    $(".user-weixin-href").attr("href", "javascript:void(0)");
    $(".user-weixin-href").attr("title", user.weixin);
    $(".user-qq-href").attr("href", "https://wpa.qq.com/msgrd?v=3&uin=" + user.qq + "&site=qq&menu=yes");
    $(".user-qq-href").attr("title", user.qq);
    $("a.user-email").attr("href", "mailto:" + user.email);
    $(".user-phone-href").attr("href", "tel:" + user.phone);
    if (user.sex == null || user.sex === "nan") {
        $(".user-sex").attr("src", "u_files/img/main_photo.jpg");
    } else {
        $(".user-sex").attr("src", "u_files/img/main_photo2.jpg");
    }
}

function changeSpecialtys(specialtys) {
    if (specialtys.length > 0) {
        $(".specialty1-name").text(specialtys[0].name);
        $(".specialty1-description").text(specialtys[0].description);
    }
    if (specialtys.length > 1) {
        $(".specialty2-name").text(specialtys[1].name);
        $(".specialty2-description").text(specialtys[1].description);
    }
}

function changeSkill(skill) {
    if (!skill || !skill.keywords) return;
    let skills = skill.keywords.split(" ");
    for (let i = 0; i < skills.length; i++) {
        let $li = $("<li>" + skills[i] + "</li>");
        $(".data-skill").append($li);
    }
}

function changeEdu(edus) {
    for (let i = 0; i < edus.length; i++) {
        let $div = $(
            '<div class="timeline-item clearfix">' +
            '  <div class="left-part">' +
            '    <h5 class="item-period">' + edus[i].start + ' <br>至<br> ' + edus[i].end + '</h5>' +
            '    <span class="item-company">' + edus[i].school + '</span>' +
            '  </div>' +
            '  <div class="divider"></div>' +
            '  <div class="right-part">' +
            '    <h4 class="item-title">' + edus[i].study + '</h4>' +
            '    <p>' + (edus[i].description || '') + '</p>' +
            '  </div>' +
            '</div>'
        );
        $(".data-edu").prepend($div);
    }
}

function changeWork(works) {
    for (let i = 0; i < works.length; i++) {
        let start = works[i].start || '';
        let end = works[i].end || '';
        let $div = $(
            '<div class="timeline-item clearfix">' +
            '  <div class="left-part">' +
            '    <h5 class="item-period">' + start + ' <br>至<br> ' + end + '</h5>' +
            '    <span class="item-company">' + works[i].company + '</span>' +
            '  </div>' +
            '  <div class="divider"></div>' +
            '  <div class="right-part">' +
            '    <h4 class="item-title">' + works[i].job + '</h4>' +
            '    <p>' + (works[i].description || '') + '</p>' +
            '  </div>' +
            '</div>'
        );
        $(".data-work").prepend($div);
    }
}
