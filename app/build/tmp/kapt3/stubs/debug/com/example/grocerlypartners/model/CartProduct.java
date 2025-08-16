package com.example.grocerlypartners.model;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0018\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001BC\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0007\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u00a2\u0006\u0004\b\r\u0010\u000eJ\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0007H\u00c6\u0003J\t\u0010 \u001a\u00020\nH\u00c6\u0003J\t\u0010!\u001a\u00020\fH\u00c6\u0003JE\u0010\"\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u00c6\u0001J\u0006\u0010#\u001a\u00020\u0005J\u0013\u0010$\u001a\u00020%2\b\u0010&\u001a\u0004\u0018\u00010\'H\u00d6\u0003J\t\u0010(\u001a\u00020\u0005H\u00d6\u0001J\t\u0010)\u001a\u00020\u0007H\u00d6\u0001J\u0016\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020\u0005R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0016R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001b\u00a8\u0006/"}, d2 = {"Lcom/example/grocerlypartners/model/CartProduct;", "Landroid/os/Parcelable;", "product", "Lcom/example/grocerlypartners/model/Product;", "quantity", "", "deliveryDate", "", "deliveredDate", "orderStatus", "Lcom/example/grocerlypartners/utils/OrderStatus;", "cancellationInfo", "Lcom/example/grocerlypartners/model/CancellationInfo;", "<init>", "(Lcom/example/grocerlypartners/model/Product;ILjava/lang/String;Ljava/lang/String;Lcom/example/grocerlypartners/utils/OrderStatus;Lcom/example/grocerlypartners/model/CancellationInfo;)V", "getProduct", "()Lcom/example/grocerlypartners/model/Product;", "getQuantity", "()I", "setQuantity", "(I)V", "getDeliveryDate", "()Ljava/lang/String;", "getDeliveredDate", "getOrderStatus", "()Lcom/example/grocerlypartners/utils/OrderStatus;", "getCancellationInfo", "()Lcom/example/grocerlypartners/model/CancellationInfo;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "describeContents", "equals", "", "other", "", "hashCode", "toString", "writeToParcel", "", "dest", "Landroid/os/Parcel;", "flags", "app_debug"})
@kotlinx.parcelize.Parcelize()
public final class CartProduct implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull()
    private final com.example.grocerlypartners.model.Product product = null;
    private int quantity;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String deliveryDate = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String deliveredDate = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.grocerlypartners.utils.OrderStatus orderStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.grocerlypartners.model.CancellationInfo cancellationInfo = null;
    
    @java.lang.Override()
    public final int describeContents() {
        return 0;
    }
    
    @java.lang.Override()
    public final void writeToParcel(@org.jetbrains.annotations.NotNull()
    android.os.Parcel dest, int flags) {
    }
    
    public CartProduct(@org.jetbrains.annotations.NotNull()
    com.example.grocerlypartners.model.Product product, int quantity, @org.jetbrains.annotations.NotNull()
    java.lang.String deliveryDate, @org.jetbrains.annotations.NotNull()
    java.lang.String deliveredDate, @org.jetbrains.annotations.NotNull()
    com.example.grocerlypartners.utils.OrderStatus orderStatus, @org.jetbrains.annotations.NotNull()
    com.example.grocerlypartners.model.CancellationInfo cancellationInfo) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.grocerlypartners.model.Product getProduct() {
        return null;
    }
    
    public final int getQuantity() {
        return 0;
    }
    
    public final void setQuantity(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeliveryDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeliveredDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.grocerlypartners.utils.OrderStatus getOrderStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.grocerlypartners.model.CancellationInfo getCancellationInfo() {
        return null;
    }
    
    public CartProduct() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.grocerlypartners.model.Product component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.grocerlypartners.utils.OrderStatus component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.grocerlypartners.model.CancellationInfo component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.grocerlypartners.model.CartProduct copy(@org.jetbrains.annotations.NotNull()
    com.example.grocerlypartners.model.Product product, int quantity, @org.jetbrains.annotations.NotNull()
    java.lang.String deliveryDate, @org.jetbrains.annotations.NotNull()
    java.lang.String deliveredDate, @org.jetbrains.annotations.NotNull()
    com.example.grocerlypartners.utils.OrderStatus orderStatus, @org.jetbrains.annotations.NotNull()
    com.example.grocerlypartners.model.CancellationInfo cancellationInfo) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}