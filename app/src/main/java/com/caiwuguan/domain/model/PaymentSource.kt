package com.caiwuguan.domain.model

enum class PaymentSource(val displayName: String) {
    WECHAT("微信"),
    ALIPAY("支付宝"),
    BANK_ICBC("工商银行"),
    BANK_CCB("建设银行"),
    BANK_ABC("农业银行"),
    BANK_BOC("中国银行"),
    BANK_COMM("交通银行"),
    BANK_CMB("招商银行"),
    CASH("现金"),
    OTHER("其他")
}
