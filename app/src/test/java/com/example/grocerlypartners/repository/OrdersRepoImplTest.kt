package com.example.grocerlypartners.repository

import com.example.grocerlypartners.model.CancellationInfo
import com.example.grocerlypartners.model.CartProduct
import com.example.grocerlypartners.model.Order
import com.example.grocerlypartners.model.Product
import com.example.grocerlypartners.repository.remote.OrdersRepoImpl
import com.example.grocerlypartners.utils.CancellationStatus
import com.example.grocerlypartners.utils.NetworkResult
import com.example.grocerlypartners.utils.OrderStatus
import com.example.grocerlypartners.utils.ProductCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

class OrdersRepoImplTest {


        // 1. Mock all external dependencies
        @Mock
        private lateinit var auth: FirebaseAuth

        @Mock
        private lateinit var db: FirebaseFirestore

        @Mock
        private lateinit var currentUser: FirebaseUser

        @Mock
        private lateinit var collectionReference: CollectionReference

        @Mock
        private lateinit var documentReference: DocumentReference

        // The class we are testing
        private lateinit var ordersRepo: OrdersRepoImpl

        private val TEST_USER_ID = "test_seller_123"
        private val OTHER_USER_ID = "other_seller_456"

        @Before
        fun setUp() {
            // Initialize mocks created with the @Mock annotation
            MockitoAnnotations.openMocks(this)

            // 2. Define the behavior of the mocked dependencies
            whenever(auth.currentUser).thenReturn(currentUser)
            whenever(currentUser.uid).thenReturn(TEST_USER_ID)
            whenever(db.collection(any())).thenReturn(collectionReference)
            whenever(collectionReference.document(TEST_USER_ID)).thenReturn(documentReference)
            whenever(documentReference.collection(any())).thenReturn(collectionReference)

            // 3. Initialize the repository with the mocks
            ordersRepo = OrdersRepoImpl(auth, db)
        }




    @Test
    fun `fetchCancelledItems returns correctly filtered map on success`() = runTest {
        // ARRANGE: Create mock data with various items to test all filter conditions

        // Item that SHOULD be in the result map
        val cancelledAcceptedItem = CartProduct(
            product = Product(partnerId = TEST_USER_ID, productId = "#543535353", image = "", itemName = "Maango", itemPrice = 200, category = ProductCategory.FruitsandVegies),
            orderStatus = OrderStatus.ACCEPTED,
            cancellationInfo = CancellationInfo(cancellationStatus = CancellationStatus.Cancelled)
        )
        // Item that SHOULD be in the result map
        val cancelledReadyItem = CartProduct(
            product = Product(partnerId = TEST_USER_ID, productId = "#543535353", image = "", itemName = "Apple", itemPrice = 200, category = ProductCategory.FruitsandVegies),
            orderStatus = OrderStatus.READY,
            cancellationInfo = CancellationInfo(cancellationStatus = CancellationStatus.Cancelled)
        )
        // Item to be filtered out (wrong partnerId)
        val otherPartnerCancelledItem = CartProduct(
            product = Product(partnerId = TEST_USER_ID, productId = "#543535353", image = "", itemName = "Orange", itemPrice = 200, category = ProductCategory.FruitsandVegies),
            orderStatus = OrderStatus.ACCEPTED,
            cancellationInfo = CancellationInfo(cancellationStatus = CancellationStatus.Cancelled)
        )
        // Item to be filtered out (wrong cancellation status)
        val nonCancelledItem = CartProduct(
            product = Product(partnerId = TEST_USER_ID, productId = "#543535353", image = "", itemName = "Orange", itemPrice = 200, category = ProductCategory.FruitsandVegies),
            orderStatus = OrderStatus.ACCEPTED,
            cancellationInfo = CancellationInfo(cancellationStatus = CancellationStatus.Non_Cancelled)
        )
        // Item to be filtered out (wrong order status)
        val pendingCancelledItem = CartProduct(
            product = Product(partnerId = TEST_USER_ID, productId = "#543535353", image = "", itemName = "Mangoostin", itemPrice = 200, category = ProductCategory.FruitsandVegies),
            orderStatus = OrderStatus.PENDING,
            cancellationInfo = CancellationInfo(cancellationStatus = CancellationStatus.Cancelled)
        )

        // Order 1 contains items that should be returned
        val order1 = Order(
            orderId = "order_1",
            items = listOf(cancelledAcceptedItem, cancelledReadyItem, nonCancelledItem)
        )
        // Order 2 contains items that should be filtered out
        val order2 = Order(
            orderId = "order_2",
            items = listOf(otherPartnerCancelledItem, pendingCancelledItem)
        )

        // Mock the Firestore snapshot and documents
        val mockDoc1: DocumentSnapshot = mock()
        val mockDoc2: DocumentSnapshot =mock()
        whenever(mockDoc1.toObject(Order::class.java)).thenReturn(order1)
        whenever(mockDoc2.toObject(Order::class.java)).thenReturn(order2)

        val mockSnapshot: QuerySnapshot = mock()
        whenever(mockSnapshot.isEmpty).thenReturn(false)
        whenever(mockSnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

        // Mock the addSnapshotListener to immediately return our mockSnapshot
        doAnswer { invocation ->
            val listener = invocation.getArgument<com.google.firebase.firestore.EventListener<QuerySnapshot>>(0)
            listener.onEvent(mockSnapshot, null)
            // Return a mock ListenerRegistration
            org.mockito.Mockito.mock(com.google.firebase.firestore.ListenerRegistration::class.java)
        }.whenever(collectionReference).addSnapshotListener(any())


        // ACT: Collect the first result from the flow
        val result = ordersRepo.fetchCancelledItems().first()

        // ASSERT: Verify the result is correct
        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data

        // Check the contents for order_1
        val orderItems = data?.get("order_1").orEmpty()
        assertTrue("order_1 should contain cancelledAcceptedItem", orderItems.any { it.product.itemName == "Maango" && it.orderStatus == OrderStatus.ACCEPTED && it.cancellationInfo.cancellationStatus == CancellationStatus.Cancelled })
        assertTrue(orderItems.any { it.product.itemName == "Maango" && it.orderStatus == OrderStatus.ACCEPTED })
        assertTrue(data?.get("order_1")?.contains(cancelledAcceptedItem) == true)
        assertTrue(data?.get("order_1")?.contains(cancelledReadyItem) == true)

        // Check the contents for order_2 (should be an empty list as all its items were filtered out)
        assertEquals(1, data?.get("order_2")?.size)
    }

}