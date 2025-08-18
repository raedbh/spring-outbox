# Outbox Pattern with Spring Boot and Debezium

*How to implement reliable messaging in distributed systems without headaches*

---

## Why the Transactional Outbox Pattern?

The transactional outbox pattern solves the dual-write problem by persisting events alongside business data in a single transaction.

For more details on the outbox pattern and possible implementations, see [Revisiting the Outbox Pattern](https://www.decodable.co/blog/revisiting-the-outbox-pattern).

While the pattern is well-established, implementing it from scratch involves a lot of boilerplate code, CDC (Change Data Capture) setup, and infrastructure complexity.

## Introducing Spring Outbox

[Spring Outbox](https://github.com/raedbh/spring-outbox) is a lightweight, Spring-native library that eliminates the complexity of implementing the transactional outbox pattern. With just a few annotations and minimal configuration, you get:

- ✅ **Transactional safety** - Data consistency is guaranteed
- ✅ **Spring Boot auto-configuration** - Works out of the box
- ✅ **Multiple database support** - Relational databases and MongoDB
- ✅ **Pre-built Debezium connectors** - For MySQL, PostgreSQL and MongoDB
- ✅ **Message broker integration** - RabbitMQ and Kafka support
- ✅ **Domain-driven design** - Aggregate-focused API

## Implementation Key Considerations

- When a domain event occurs, it generates one or more messages.
- Each outbox entry represents either an event or a command.
- An operation, part of an event, represents the action performed on the root entity (e.g., create, update, award) and helps consumers determine if deserialization is needed.
- Decouple the outbox message producer and consumer to enable scalability and independent evolution.
- Keep the Debezium connector simple by focusing on reading outbox entries and producing messages.

## Building Source-to-Pay System

Let's build a realistic procurement system that demonstrates Spring Outbox in action. Our **Source-to-Pay (S2P)** system will handle:

- **RFP Management**: Create and publish Request for Proposals with requirements
- **Vendor Proposals**: Vendors submit proposals with pricing and other details
- **Review Process**: Proposals go through a structured review workflow
- **Award Process**: When a proposal is awarded, related messages are produced
- **Service Integration**: Email notifications via dedicated service

> **💡 Complete Source Code**: The full implementation is available at [github.com/raedbh/spring-outbox/tree/main/spring-outbox-sample](https://github.com/raedbh/spring-outbox/tree/main/spring-outbox-sample)


### Architecture Overview

![Sample Architecture Overview](https://raw.githubusercontent.com/raedbh/spring-outbox/main/spring-outbox-sample/sample_architecture_overview.png)

## Implementation Steps

Here are the essential steps to integrate Spring Outbox into your application, demonstrated through our S2P system:

## Step 1: Project Setup

First, let's create a Spring Boot project with the necessary dependencies:

```xml
<dependency>
    <groupId>io.github.raedbh</groupId>
    <artifactId>spring-outbox-jpa</artifactId>
    <version>0.7.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

## Step 2: Enable Spring Outbox

Configure your main application class:

```java
@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = OutboxJpaRepositoryFactoryBean.class)
public class S2PApplication {
    public static void main(String[] args) {
        SpringApplication.run(S2PApplication.class, args);
    }
}
```

Add configuration properties:

```properties
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/s2p
spring.datasource.username=<db_username>
spring.datasource.password=<db_password>

# Enable automatic outbox table creation
spring.outbox.relational.auto-create=true

# RabbitMQ configuration
spring.rabbitmq.host=127.0.0.1
spring.rabbitmq.username=<rabbitmq_username>
spring.rabbitmq.password=<rabbitmq_password>
```

## Step 3: Create Your Domain Model

### The Proposal Aggregate

```java
@Entity
@Table(name = "proposals")
public class Proposal extends RootEntity implements AggregateRoot<Proposal, EntityIdentifier> {
    
    private final EntityIdentifier id;
    private final Association<RequestForProposal, EntityIdentifier> rfp;
    private final Association<Vendor, EntityIdentifier> vendor;
    private final String details;
    private final MonetaryAmount proposalAmount;
    private Status status;
    
    // Constructors and getters...
    
    /**
     * Mark the proposal as awarded with vendor contact for notifications.
     */
    public Proposal markAwarded(VendorContact vendorContact) {
        Assert.state(this.status == Status.UNDER_REVIEW,
          "Cannot award a proposal that is not under review!");
        
        this.status = Status.AWARDED;
        
        // Create email notification command
        Map<String, Serializable> templateParams = Map.of(
            "proposalAmount", this.proposalAmount.getNumber().toString()
            // ... other template parameters
        );
        EmailNotification notification = new EmailNotification(
            "proposal-awarded", vendorContact, templateParams);
        
        // 🎯 Assign event with command - both will be persisted atomically
        assignEvent(new ProposalAwarded(this, notification));
        
        return this;
    }
    
    public enum Status {
        CREATED, SUBMITTED, UNDER_REVIEW, AWARDED, REJECTED
    }
}
```

### The Domain Event and Command

```java
public class ProposalAwarded extends EventOutboxed<Proposal> {
    
    public ProposalAwarded(Proposal source, EmailNotification command) {
        super(source, command);
    }
    
    @Override
    public String getOperation() {
        return "award"; // This helps consumers determine the type of operation
    }
}
```

```java
public record EmailNotification(
  String type,
  VendorContact to,
  Map<String, Serializable> templateParams
) implements CommandOutboxed {}
```

## Step 4: Implement the Domain Service

```java
@Service
@Transactional
public class ProposalManagement {
    
    private final ProposalRepository proposalRepository;
    
    public ProposalManagement(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }
    
    public Proposal awardProposal(EntityIdentifier proposalId, VendorContact vendorContact) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new ProposalNotFoundException(proposalId));
        
        // When this transaction commits, Spring Outbox will atomically:
        // 1. Save the proposal changes
        // 2. Insert an outbox record for the ProposalAwarded event
        // 3. Insert an outbox record for the EmailNotification command
        return proposalRepository.save(proposal.markAwarded(vendorContact));
    }
}
```

## Step 5: Set Up Change Data Capture

The Debezium connector detects new entries in the outbox table and transmits them to RabbitMQ. Follow these steps:

1. **Start MySQL and RabbitMQ** (using Docker or your preferred method)

2. **Create the required RabbitMQ infrastructure:**
   - `s2p.topic` default topic exchange for S2P System
   - `rfp.proposals` queue bound to exchange with `proposal.#` routing key pattern
   - `emails` queue for email notification

3. **Run the Debezium connector** using the Docker command:

```bash
docker run -d \
  -e SPRING_OUTBOX_CONNECTOR_DATABASE_HOSTNAME=<db_host> \
  -e SPRING_OUTBOX_CONNECTOR_DATABASE_DBNAME=<db_name> \
  -e SPRING_OUTBOX_CONNECTOR_DATABASE_USER=<db_user> \
  -e SPRING_OUTBOX_CONNECTOR_DATABASE_PASSWORD=<db_password> \
  -e SPRING_OUTBOX_CONNECTOR_RABBIT_MESSAGES_PROPOSALAWARDED_EXCHANGE=s2p.topic \
  -e SPRING_OUTBOX_CONNECTOR_RABBIT_MESSAGES_PROPOSALAWARDED_ROUTINGKEY=proposal.awarded \
  -e SPRING_OUTBOX_CONNECTOR_RABBIT_MESSAGES_EMAILNOTIFICATION_ROUTINGKEY=emails \
  -e SPRING_RABBITMQ_HOST=<rabbit_host> \
  -e SPRING_RABBITMQ_USERNAME=<rabbit_user> \
  -e SPRING_RABBITMQ_PASSWORD=<rabbit_password> \
  --net host \
  --name spring-outbox-debezium-connector \
  raed/spring-outbox-debezium-connector-mysql-rabbit:0.7.0
```

> **Note**: The `<db_user>` requires at least one of the `SUPER` or `REPLICATION CLIENT` privileges to read from the MySQL binary log.

## Step 6: REST Endpoint Example

```java
@RestController
@RequestMapping("/proposals")
public class ProposalController {
    
    private final ProposalManagement proposalManagement;
    
    @PostMapping("/{id}/award")
    public ResponseEntity<ProposalResponse> awardProposal(@PathVariable EntityIdentifier id) {
        Proposal awarded = proposalManagement.awardProposal(id);
        return ResponseEntity.ok(ProposalResponse.from(awarded));
    }
    
    // Other endpoints...
}
```

## Step 7: Consume Events and Commands

Spring Outbox publishes both events and commands. First, add the messaging dependency to consuming components:

```xml
<dependency>
    <groupId>io.github.raedbh</groupId>
    <artifactId>spring-outbox-rabbit</artifactId>
    <version>0.7.0</version>
</dependency>
```

Here's how different components consume the messages:

### Event Consumption (Within S2P App)

```java
@Component
class RfpEventHandlers {

    @Component
    static class ProposalAwardHandler {

        private final RfpManagement rfpManagement;

        ProposalAwardHandler(RfpManagement rfpManagement) {
            this.rfpManagement = rfpManagement;
        }

        @RabbitListener(queues = "rfp.proposals")
        void onProposalAwarded(@OutboxMessageBody(operation = "award") Optional<ProposalMessageBody> messageBody) {
            messageBody.ifPresent(body -> {
                log.info("Received proposal awarded event for RFP: {}", body.rfpId());
                rfpManagement.close(EntityIdentifier.fromString(body.rfpId()));
            });
        }
    }
}
```

### Command Processing (Email Service)

```java
@Component
public class EmailNotificationHandler {

    private final EmailSender emailSender;

    @RabbitListener(queues = "emails")
    public void handleEmailNotification(@OutboxMessageBody Optional<EmailMessageBody> messageBody) {
        messageBody.ifPresent(email -> {
            log.info("Received email notification for type: {} to: {}", 
              email.getType(), email.getTo());
            emailSender.send(email);
        });
    }
}
```

## What Happens When You Award a Proposal?

When you call the `awardProposal` method:

1. **Transactional Write**: The proposal status and outbox entries (event + command) are saved atomically
2. **Change Capture**: Debezium detects the outbox table changes via MySQL binlog
3. **Message Publishing**: The connector routes messages to appropriate RabbitMQ queues
4. **Message Processing**: Both the RFP module and Email Service process their respective messages

This demonstrates how Spring Outbox handles reliable, transactional event publishing.


## Is Spring Outbox Right for Your Project?

Spring Outbox is designed for specific use cases and may not be suitable for every project. Consider using it when:

✅ **You have RabbitMQ or Kafka** - Currently supports these message brokers
✅ **Reliability is critical** - You need to ensure event messages are always consistent with your database changes
✅ **Event-driven architecture** - Your system benefits from decoupled, event-based communication
✅ **Cross-service integration** - You need to trigger actions in multiple independent services or internal components reliably
✅ **CDC infrastructure available** - You can run Debezium connectors in your environment

**Not recommended for:**
❌ Simple applications without complex integration needs
❌ Systems where eventual consistency isn't acceptable

## What's Next?

Spring Outbox is actively under development. **Version 1.0 will be released soon** with enhanced stability and additional capabilities.

**Get involved** - your feedback and contributions help improve Spring Outbox!
