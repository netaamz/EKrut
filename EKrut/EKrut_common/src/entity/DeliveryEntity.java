package entity;

import java.io.Serializable;

import enums.CustomerStatusEnum;
import enums.DeliveryStatusEnum;

public class DeliveryEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String estimatedTime;
	private int orderId;
	private String address, region; //change later to AddressEntity
	private DeliveryStatusEnum deliveryStatus;
	private CustomerStatusEnum customerStatus;

	

	/**
	 * For getting delivery entity
	 * @param orderId  order id
	 * @param region order region
	 * @param address order address
	 * @param estimatedTime order estimated time
	 * @param deliveryStatusEnum order delivery status
	 * @param customerStatusEnum order  customer status
	 */
	public DeliveryEntity(int orderId, String region,  String address, String estimatedTime,
			DeliveryStatusEnum deliveryStatusEnum, CustomerStatusEnum customerStatusEnum) {
		super();
		this.estimatedTime = estimatedTime;
		this.orderId = orderId;
		this.address = address;
		this.region = region;
		this.deliveryStatus = deliveryStatusEnum;
		this.customerStatus = customerStatusEnum;
	}

	/**
	 * For build new delivery entity
	 * @param region customer region
	 * @param address customer address
	 */
	public DeliveryEntity(String region, String address) {
		this.address=address;
		this.region = region;
		this.deliveryStatus = DeliveryStatusEnum.pendingApproval;
		this.customerStatus= CustomerStatusEnum.NOT_APPROVED;
	}


	/**
	 * Gets the estimated time.
	 *
	 * @return the estimated time
	 */
	public String getEstimatedTime() {
		return estimatedTime;
	}


	/**
	 * Sets the estimated time.
	 *
	 * @param estimatedTime the new estimated time
	 */
	public void setEstimatedTime(String estimatedTime) {
		this.estimatedTime = estimatedTime;
	}


	/**
	 * Gets the order id.
	 *
	 * @return the order id
	 */
	public int getOrderId() {
		return orderId;
	}


	/**
	 * Sets the order id.
	 *
	 * @param orderId the new order id
	 */
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}


	/**
	 * Gets the address.
	 *
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}


	/**
	 * Sets the address.
	 *
	 * @param address the new address
	 */
	public void setAddress(String address) {
		this.address = address;
	}


	/**
	 * Gets the region.
	 *
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}


	/**
	 * Sets the region.
	 *
	 * @param region the new region
	 */
	public void setRegion(String region) {
		this.region = region;
	}


	/**
	 * Gets the delivery status.
	 *
	 * @return the delivery status
	 */
	public DeliveryStatusEnum getDeliveryStatus() {
		return deliveryStatus;
	}


	/**
	 * Sets the delivery status.
	 *
	 * @param deliveryStatusEnum the new delivery status
	 */
	public void setDeliveryStatus(DeliveryStatusEnum deliveryStatusEnum) {
		this.deliveryStatus = deliveryStatusEnum;
	}


	/**
	 * Gets the customer status.
	 *
	 * @return the customer status
	 */
	public CustomerStatusEnum getCustomerStatus() {
		return customerStatus;
	}


	/**
	 * Sets the customer status.
	 *
	 * @param customerStatusEnum the new customer status
	 */
	public void setCustomerStatus(CustomerStatusEnum customerStatusEnum) {
		this.customerStatus = customerStatusEnum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + orderId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeliveryEntity other = (DeliveryEntity) obj;
		if (orderId != other.orderId)
			return false;
		return true;
	}
	

	
}
